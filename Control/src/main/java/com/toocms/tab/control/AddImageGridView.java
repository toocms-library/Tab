package com.toocms.tab.control;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.toocms.tab.imageloader.ImageLoader;
import com.toocms.tab.toolkit.ListUtils;
import com.toocms.tab.toolkit.ScreenUtils;
import com.toocms.tab.toolkit.Toolkit;
import com.toocms.tab.toolkit.configs.Settings;

import java.io.File;
import java.util.ArrayList;

/**
 * 添加图片的GridView<br/>
 * 需要在{@link android.app.Activity#onActivityResult(int, int, Intent)}中添加类似如下代码 <br/>
 * case Constants.SELECT_IMAGE: <br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;if (arg2 != null) { <br/>
 * gridView.getAdapter().display(list); <br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;} <br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;break; <br/>
 *
 * @author Zero
 * <p/>
 * 2015年1月21日
 */
public class AddImageGridView extends GridView implements OnItemClickListener {

    public interface OnDeleteItemListener {
        public void onDeleteItem(String url);
    }

    private final int NUM_COLIMNS = 4;

    public AddImageAdapter adapter;

    private ArrayList<String> list;
    private RelativeLayout.LayoutParams params;

    private OnDeleteItemListener listener;

    private int maxImageNum = 9;
    private int numColumns = NUM_COLIMNS;
    private int verticalSpacing = ScreenUtils.dpToPxInt(10);
    private int horizontalSpacing = ScreenUtils.dpToPxInt(10);
    private int itemSize = (Settings.displayWidth - (horizontalSpacing * numColumns + 1)) / numColumns;
    private int verticalSize = itemSize;
    private int horizontalSize = itemSize;

    public AddImageGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        parseStyle(context, attrs);
        init();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (adapter != null) {
//            ((BaseActivity) AppManager.getInstance().getTopActivity()).startSelectMultiImageAty(null, maxImageNum - ListUtils.getSize(list));
        }
    }

    public AddImageAdapter getAdapter() {
        return adapter;
    }

    private void init() {
        setNumColumns(numColumns);
        setVerticalSpacing(verticalSpacing);
        setHorizontalSpacing(horizontalSpacing);
        adapter = new AddImageAdapter();
        setAdapter(adapter);
        setOnItemClickListener(this);
    }

    private void parseStyle(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.AddImageGridView);
        if (typedArray.hasValue(R.styleable.AddImageGridView_horizontal_size) &&
                typedArray.hasValue(R.styleable.AddImageGridView_vertical_size)) {
            horizontalSize = (int) typedArray.getDimension(R.styleable.AddImageGridView_horizontal_size, itemSize);
            verticalSize = (int) typedArray.getDimension(R.styleable.AddImageGridView_vertical_size, itemSize);
        }
    }

    /**
     * 设置最多可添加的图片数量
     *
     * @param maxImageNum 图片的数量，默认为9
     */
    public void setMaxImageNum(int maxImageNum) {
        this.maxImageNum = maxImageNum;
    }

    @Override
    public void setNumColumns(int numColumns) {
        this.numColumns = numColumns;
        super.setNumColumns(numColumns);
    }

    public void setSpacing(int horizontalSpacing, int verticalSpacing) {
        this.horizontalSpacing = horizontalSpacing;
        this.verticalSpacing = verticalSpacing;
        setHorizontalSpacing(horizontalSpacing);
        setVerticalSpacing(verticalSpacing);
    }

    public void setImageSize(int horizontalSize, int verticalSize) {
        this.horizontalSize = horizontalSize;
        this.verticalSize = verticalSize;
    }

    public void setOnDeleteItemListener(OnDeleteItemListener listener) {
        this.listener = listener;
    }

    public class AddImageAdapter extends BaseAdapter {

        private ViewHolder viewHolder;
        private RelativeLayout.LayoutParams params;

        public AddImageAdapter() {
            params = new RelativeLayout.LayoutParams(horizontalSize, verticalSize);
            params.setMargins(0, 15, 15, 0);
        }

        @Override
        public int getCount() {
            return ListUtils.getSize(list) < maxImageNum ? ListUtils.getSize(list) + 1 : ListUtils.getSize(list);
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(getContext(), R.layout.griditem_addimage_image, null);
                viewHolder = new ViewHolder();
                viewHolder.imageView = convertView.findViewById(R.id.griditem_addimage_imgv);
                viewHolder.imageView.setLayoutParams(params);
                viewHolder.imgDelete = convertView.findViewById(R.id.griditem_addimage_delete);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            //
            if (ListUtils.getSize(list) == 0 || ListUtils.getSize(list) == position) {
                ImageLoader.loadResId2Image(R.drawable.image_show_piceker_add, viewHolder.imageView, 0);
                viewHolder.imgDelete.setVisibility(INVISIBLE);
            } else {
                if (Toolkit.isUrl(list.get(position))) {
                    ImageLoader.loadUrl2Image(list.get(position), viewHolder.imageView, 0);
                } else {
                    ImageLoader.loadFile2Image(new File(list.get(position)), viewHolder.imageView, 0);
                }
                viewHolder.imgDelete.setVisibility(VISIBLE);
            }
            viewHolder.imgDelete.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) listener.onDeleteItem(list.get(position));
                    list.remove(position);
                    notifyDataSetChanged();
                }
            });
            return convertView;
        }

//        /**
//         * 是否是最后的
//         */
//        public boolean isLastPosition(int position) {
//            return ListUtils.getSize(list) < maxImageNum && position == getCount() - 1;
//        }

        public void display(ArrayList<String> list) {
            AddImageGridView.this.list = list;
            notifyDataSetChanged();
        }

        private class ViewHolder {
            public ImageView imageView;
            public ImageView imgDelete;
        }
    }
}
