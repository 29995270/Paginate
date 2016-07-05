package com.paginate.recycler;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ViewGroup;

class WrapperAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int ITEM_VIEW_TYPE_LOADING = Integer.MAX_VALUE - 50; // Magic
    private static final int ITEM_VIEW_TYPE_ERROR = Integer.MAX_VALUE - 51; // Magic
    private static final int ITEM_VIEW_TYPE_END = Integer.MAX_VALUE - 52; // Magic

    private final RecyclerView.Adapter wrappedAdapter;
    private final LoadingListItemCreator loadingListItemCreator;
    private final LoadingListItemCreator errorListItemCreator;
    private final LoadingListItemCreator endListItemCreator;
    private boolean displayLoadingRow = true;
    private boolean displayErrorRow = false;
    private boolean displayEndRow = false;

    public WrapperAdapter(RecyclerView.Adapter adapter, LoadingListItemCreator loading, LoadingListItemCreator error, LoadingListItemCreator end) {
        this.wrappedAdapter = adapter;
        this.loadingListItemCreator = loading;
        this.errorListItemCreator = error;
        this.endListItemCreator = end;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_VIEW_TYPE_LOADING) {
            return loadingListItemCreator.onCreateViewHolder(parent, viewType);
        } else if (viewType == ITEM_VIEW_TYPE_ERROR){
            if (errorListItemCreator == null) return null;
            return errorListItemCreator.onCreateViewHolder(parent, viewType);
        } else if (viewType == ITEM_VIEW_TYPE_END) {
            if (endListItemCreator == null) return null;
            return endListItemCreator.onCreateViewHolder(parent, viewType);
        } else {
            return wrappedAdapter.onCreateViewHolder(parent, viewType);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (isLoadingRow(position)) {
            int itemViewType = holder.getItemViewType();
            if (itemViewType == ITEM_VIEW_TYPE_LOADING) {
                loadingListItemCreator.onBindViewHolder(holder, position);
            } else if (itemViewType == ITEM_VIEW_TYPE_ERROR){
                if (errorListItemCreator == null) return;
                errorListItemCreator.onBindViewHolder(holder, position);
            } else if (itemViewType == ITEM_VIEW_TYPE_END) {
                if (endListItemCreator == null) return;
                endListItemCreator.onBindViewHolder(holder, position);
            }
        } else {
            wrappedAdapter.onBindViewHolder(holder, position);
        }
    }

    @Override
    public int getItemCount() {
        return (displayLoadingRow || displayErrorRow || displayEndRow) ? wrappedAdapter.getItemCount() + 1 : wrappedAdapter.getItemCount();
    }

    @Override
    public int getItemViewType(int position) {
        if (isLoadingRow(position)) {
            if (displayLoadingRow) {
                return ITEM_VIEW_TYPE_LOADING;
            } else if (displayErrorRow) {
                return ITEM_VIEW_TYPE_ERROR;
            } else if (displayEndRow) {
                return ITEM_VIEW_TYPE_END;
            }
        }
        return wrappedAdapter.getItemViewType(position);
    }

    @Override
    public long getItemId(int position) {
        return isLoadingRow(position) ? RecyclerView.NO_ID : wrappedAdapter.getItemId(position);
    }

    @Override
    public void setHasStableIds(boolean hasStableIds) {
        super.setHasStableIds(hasStableIds);
        wrappedAdapter.setHasStableIds(hasStableIds);
    }

    public RecyclerView.Adapter getWrappedAdapter() {
        return wrappedAdapter;
    }

    boolean isDisplayLoadingRow() {
        return displayLoadingRow;
    }

    void displayLoadingRow(boolean displayLoadingRow) {
        if (this.displayLoadingRow != displayLoadingRow) {
            this.displayLoadingRow = displayLoadingRow;
            notifyDataSetChanged();
        }
    }

    void displayFooterRow(boolean displayLoadingRow, boolean displayErrorRow, boolean displayEndRow) {
        int footerCount = (this.displayLoadingRow || this.displayErrorRow || this.displayEndRow)? 1: 0;
        if (this.displayLoadingRow != displayLoadingRow || this.displayErrorRow != displayErrorRow || this.displayEndRow != displayEndRow) {
            this.displayLoadingRow = displayLoadingRow;
            this.displayErrorRow = displayErrorRow && errorListItemCreator != null;
            this.displayEndRow = displayEndRow && endListItemCreator != null;

            int newFooterCount = (displayLoadingRow || displayErrorRow || displayEndRow)? 1: 0;
            notifyFooter(footerCount != newFooterCount);
        }
    }

    void notifyFooter(boolean addOrChange) {
        if (addOrChange) {
            notifyItemInserted(getItemCount() - 1);
        } else {
            notifyItemChanged(getLoadingRowPosition());
        }
    }

    boolean isLoadingRow(int position) {
        return (displayLoadingRow || displayErrorRow || displayEndRow) && position == getLoadingRowPosition();
    }

    private int getLoadingRowPosition() {
        return (displayLoadingRow || displayErrorRow || displayEndRow) ? getItemCount() - 1 : -1;
    }
}