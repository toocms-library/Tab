package cn.zero.android.common.view.navigation;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.toocms.frame.image.ImageLoader;
import com.toocms.frame.tool.Toolkit;
import com.toocms.frame.ui.R;
import com.zhy.autolayout.utils.AutoUtils;

import org.xutils.x;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import cn.zero.android.common.util.ListUtils;

/**
 * 翻页导航栏的gridview适配器
 *
 * @author Zero
 * @date 2016/9/9 15:02
 */
class NavigationAdapter<T> extends BaseAdapter {

    private OnNavigationClickListener listener;
    private LayoutInflater layoutInflater;
    private ViewHolder viewHolder;
    private RequestManager glide;

    private List<T> list;
    private String[] keys; // 图标和文字的key值

    private int index; // 页数下标
    private int page_size;
    private boolean isCoerciveCircle; // 是否强制显示圆形图片

    public NavigationAdapter(Context context, RequestManager glide, List<T> list, String[] keys, int index, int page_size, boolean isCoerciveCircle) {
        this.list = list;
        this.keys = keys;
        this.index = index;
        this.page_size = page_size;
        this.glide = glide;
        this.isCoerciveCircle = isCoerciveCircle;
        layoutInflater = LayoutInflater.from(context);
    }

    public void setOnNavigationClickListener(OnNavigationClickListener listener) {
        this.listener = listener;
    }

    /**
     * 先判断数据源的长度是否够显示满本页 → ListUtils.getSize(list) > (index + 1)
     * 如果够，直接返回每一页显示数量 → pageSize
     * 如果不够，则有几项返回几项 → ListUtils.getSize(list) - index * pageSize
     *
     * @return
     */
    @Override
    public int getCount() {
        return ListUtils.getSize(list) > (index + 1) * page_size ? page_size : ListUtils.getSize(list) - index * page_size;
    }

    @Override
    public T getItem(int position) {
        return list.get(position + index * page_size);
    }

    @Override
    public long getItemId(int position) {
        return position + index * page_size;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.listitem_navigation, parent, false);
            convertView.getLayoutParams().height = AutoUtils.getPercentHeightSize(190);
            viewHolder = new ViewHolder();
            viewHolder.imageView = convertView.findViewById(R.id.listitem_navigation_icon);
            viewHolder.textView = convertView.findViewById(R.id.listitem_navigation_text);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        String uri = getValue(getItem(position), keys[0]);
        if (Toolkit.isUrl(uri)) {
            if (isCoerciveCircle)
                ImageLoader.loadUrl2CircleImage(glide, uri, viewHolder.imageView, 0);
            else ImageLoader.loadUrl2Image(glide, uri, viewHolder.imageView, 0);
        } else {
            if (isCoerciveCircle)
                ImageLoader.loadResId2CircleImage(glide, Integer.parseInt(uri), viewHolder.imageView, 0);
            else ImageLoader.loadResId2Image(glide, Integer.parseInt(uri), viewHolder.imageView, 0);
        }
        viewHolder.textView.setText(getValue(getItem(position), keys[1]));
        // 设置监听
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null)
                    listener.onNavigationClick((int) getItemId(position));
            }
        });
        return convertView;
    }

    private String getValue(T t, String fieldName) {
        if (t instanceof Map) {
            return String.valueOf(((Map) t).get(fieldName));
        } else {
            try {
                Field field = t.getClass().getDeclaredField(fieldName);
                boolean accessFlag = field.isAccessible();
                field.setAccessible(true);
                String value = String.valueOf(field.get(t));
                field.setAccessible(accessFlag);
                return value;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    private class ViewHolder {
        public ImageView imageView;
        public TextView textView;
    }
}
