package com.toocms.frame.ui.imageselector;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListPopupWindow;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.toocms.frame.config.Settings;
import com.toocms.frame.ui.BaseActivity;
import com.toocms.frame.ui.BasePresenter;
import com.toocms.frame.ui.R;
import com.toocms.frame.ui.imageselector.adapter.FolderAdapter;
import com.toocms.frame.ui.imageselector.adapter.ImageGridAdapter;
import com.toocms.frame.ui.imageselector.bean.Folder;
import com.toocms.frame.ui.imageselector.bean.Image;
import com.toocms.frame.ui.imageselector.utils.FileUtils;

import org.xutils.common.util.LogUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.zero.android.common.permission.PermissionFail;
import cn.zero.android.common.permission.PermissionSuccess;
import cn.zero.android.common.util.FileManager;
import cn.zero.android.common.util.ImageUtils;
import cn.zero.android.common.util.ListUtils;
import cn.zero.android.common.view.ucrop.UCrop;
import cn.zero.android.common.view.ucrop.model.CropType;

/**
 * 选择图片
 *
 * @author Zero
 * @date 2016/3/28 17:48
 */
public class SelectImageAty extends BaseActivity {

    private static final int PERMISSIONS_CAMERA = 0x666;

    private static final String KEY_TEMP_FILE = "key_temp_file";

    /**
     * 最大图片选择次数，int类型，默认9
     */
    public static final String EXTRA_SELECT_COUNT = "max_select_count";

    /**
     * 图片选择模式，默认多选
     */
    public static final String EXTRA_SELECT_MODE = "select_count_mode";

    /**
     * 是否显示相机，默认显示
     */
    public static final String EXTRA_SHOW_CAMERA = "show_camera";

    /**
     * 裁剪类型，默认为正方形
     */
    public static final String EXTRA_CROP_TYPE = "extra_crop_type";

    /**
     * 裁剪比例-横向，默认为1
     */
    public static final String EXTRA_ASPECT_RATIO_X = UCrop.EXTRA_ASPECT_RATIO_X;

    /**
     * 裁剪比例-纵向，默认为1
     */
    public static final String EXTRA_ASPECT_RATIO_Y = UCrop.EXTRA_ASPECT_RATIO_Y;

    /**
     * 选择结果，返回为 ArrayList&lt;String&gt; 图片路径集合
     */
    public static final String EXTRA_RESULT = "select_result";

    /**
     * 默认选择集
     */
    public static final String EXTRA_DEFAULT_SELECTED_LIST = "default_list";

    /**
     * 单选
     */
    public static final int MODE_SINGLE = 0;

    /**
     * 多选
     */
    public static final int MODE_MULTI = 1;

    // 不同loader定义
    private static final int LOADER_ALL = 0;
    private static final int LOADER_CATEGORY = 1;
    // 请求加载系统照相机
    private static final int REQUEST_CAMERA = 100;

    // 结果数据
    private ArrayList<String> resultList = new ArrayList<>();
    // 文件夹数据
    private ArrayList<Folder> resultFolder = new ArrayList<>();

    // 图片Grid
    private GridView gridView;

    private ImageGridAdapter imageAdapter;
    private FolderAdapter folderAdapter;

    private ListPopupWindow folderPopupWindow;

    // 类别
    private TextView tvCategory;
    // 预览按钮
    private Button btnPreview;
    // 完成按钮
    private Button btnSubmit;
    // 底部View
    private View popupAnchorView;

    private Thread thread; // 图片压缩处理线程
    private File tmpFile;

    private boolean hasFolderGened = false;
    private boolean isShowCamera = false;
    private float ratioX;
    private float ratioY;
    private int desireImageCount;
    private int mode;
    // 裁剪类型
    private int cropType = CropType.TYPE_SQUARE;

    private Handler handler = new Handler(msg -> {
        removeProgress(SelectImageAty.this);
        // 返回已选择的图片数据
        Intent data = new Intent();
        data.putStringArrayListExtra(EXTRA_RESULT, resultList);
        setResult(RESULT_OK, data);
        finish();
        return false;
    });

    @Override
    protected void onCreateActivity(@Nullable Bundle savedInstanceState) {
        // 隐藏actionbar
        mActionBar.hide();
        initialized();
        // 初始化控件
        initControls();
        // 返回按钮
        findViewById(R.id.btn_back).setOnClickListener(view -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        // 完成按钮
        if (resultList == null || resultList.size() <= 0) {
            btnSubmit.setText(R.string.action_done);
            btnSubmit.setEnabled(false);
        } else {
            btnSubmit.setText(String.format("%s(%d/%d)", getString(R.string.action_done), resultList.size(), desireImageCount));
            btnSubmit.setEnabled(true);
        }
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (resultList != null && resultList.size() > 0) {
                    showProgress();
                    thread = new Thread(() -> {
                        for (int i = 0; i < ListUtils.getSize(resultList); i++) {
                            resultList.set(i, ImageUtils.compressImage(resultList.get(i)).getAbsolutePath());
                        }
                        handler.sendEmptyMessage(0);
                    });
                    thread.start();
                }
            }
        });

        // 初始化适配器
        imageAdapter = new ImageGridAdapter(this, isShowCamera, 3);
        // 是否显示选择指示器
        imageAdapter.showSelectIndicator(mode == MODE_MULTI);
        // 初始化，加载所有图片
        tvCategory.setText(R.string.folder_all);
        tvCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (folderPopupWindow == null) {
                    createPopupFolderList();
                }

                if (folderPopupWindow.isShowing()) {
                    folderPopupWindow.dismiss();
                } else {
                    folderPopupWindow.show();
                    int index = folderAdapter.getSelectIndex();
                    index = index == 0 ? index : index - 1;
                    folderPopupWindow.getListView().setSelection(index);
                }
            }
        });

        // 初始化，按钮状态初始化
        if (ListUtils.isEmpty(resultList)) {
            btnPreview.setText(R.string.preview);
            btnPreview.setEnabled(false);
        }
        btnPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO 预览
            }
        });

        // 设置gridview
        gridView.setAdapter(imageAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (imageAdapter.isShowCamera()) {
                    // 如果显示照相机，则第一个Grid显示为照相机
                    if (i == 0) {
                        requestPermissions(PERMISSIONS_CAMERA, Manifest.permission.CAMERA);
                    } else {
                        // 正常操作
                        Image image = (Image) adapterView.getAdapter().getItem(i);
                        selectImageFromGrid(image, mode);
                    }
                } else {
                    // 正常操作
                    Image image = (Image) adapterView.getAdapter().getItem(i);
                    selectImageFromGrid(image, mode);
                }
            }
        });

        folderAdapter = new FolderAdapter(this);

        // 首次加载所有图片
        getSupportLoaderManager().initLoader(LOADER_ALL, null, loaderCallback);

        if (savedInstanceState != null) {
            tmpFile = (File) savedInstanceState.getSerializable(KEY_TEMP_FILE);
        }
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.aty_image_selector;
    }

    @Override
    protected int getTitlebarResId() {
        return R.id.selector_image_titlebar;
    }

    @Override
    protected BasePresenter getPresenter() {
        return null;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(KEY_TEMP_FILE, tmpFile);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (folderPopupWindow != null) {
            if (folderPopupWindow.isShowing()) {
                folderPopupWindow.dismiss();
            }
        }
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void initialized() {
        Intent intent = getIntent();
        // 最大选择图片数量
        desireImageCount = intent.getIntExtra(EXTRA_SELECT_COUNT, 9);
        // 图片选择模式（单选/多选）
        mode = intent.getIntExtra(EXTRA_SELECT_MODE, MODE_MULTI);
        // 是否显示照相机
        isShowCamera = intent.getBooleanExtra(EXTRA_SHOW_CAMERA, true);
        // 默认选择
        if (mode == MODE_MULTI && intent.hasExtra(EXTRA_DEFAULT_SELECTED_LIST)) {
            resultList = intent.getStringArrayListExtra(EXTRA_DEFAULT_SELECTED_LIST);
        }
        // 裁剪类型
        cropType = intent.getIntExtra(EXTRA_CROP_TYPE, CropType.TYPE_SQUARE);
        // 裁剪比例
        ratioX = intent.getFloatExtra(EXTRA_ASPECT_RATIO_X, 1);
        ratioY = intent.getFloatExtra(EXTRA_ASPECT_RATIO_Y, 1);
    }

    @Override
    protected void requestData() {
    }

    private void initControls() {
        popupAnchorView = findViewById(R.id.footer);
        tvCategory = findViewById(R.id.category_btn);
        btnPreview = findViewById(R.id.preview);
        gridView = findViewById(R.id.grid);
        btnSubmit = findViewById(R.id.commit);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 相机拍照完成后，返回图片路径
        switch (resultCode) {
            case RESULT_OK:
                switch (requestCode) {
                    case REQUEST_CAMERA:
                        if (tmpFile != null) {
                            // notify system
                            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(tmpFile)));
                            // 如果是单选就进入图片裁剪
                            if (mode == MODE_SINGLE) {
                                beginCrop(tmpFile);
                            } else {
                                Intent intent = new Intent();
                                resultList.add(ImageUtils.compressImage(tmpFile.getAbsolutePath()).getAbsolutePath());
                                intent.putStringArrayListExtra(EXTRA_RESULT, resultList);
                                setResult(RESULT_OK, intent);
                                finish();
                            }
                        }
                        break;
                    case UCrop.REQUEST_CROP:
                        handleCrop(data);
                        break;
                }
                break;
            case UCrop.RESULT_ERROR:
                Throwable cropError = UCrop.getError(data);
                if (cropError != null) {
                    LogUtil.e(cropError.getMessage());
                } else {
                    LogUtil.e("纯属意外，再试一次！");
                }
                break;
            default:
                while (tmpFile != null && tmpFile.exists()) {
                    boolean success = tmpFile.delete();
                    if (success) tmpFile = null;
                }
                break;
        }
    }

    @Override
    protected int getStatusBarColor() {
        return Color.parseColor("#21282C");
    }

    /**
     * 创建弹出的ListView
     */
    private void createPopupFolderList() {
        int width = Settings.displayWidth;
        int height = (int) (Settings.displayHeight * (4.8f / 8.0f));
        folderPopupWindow = new ListPopupWindow(this);
        folderPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        folderPopupWindow.setAdapter(folderAdapter);
        folderPopupWindow.setContentWidth(width);
        folderPopupWindow.setWidth(width);
        folderPopupWindow.setHeight(height);
        folderPopupWindow.setAnchorView(popupAnchorView);
        folderPopupWindow.setModal(true);
        folderPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                folderAdapter.setSelectIndex(i);
                final int index = i;
                final AdapterView v = adapterView;

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        folderPopupWindow.dismiss();

                        if (index == 0) {
                            LoaderManager.getInstance(SelectImageAty.this).restartLoader(LOADER_ALL, null, loaderCallback);
                            tvCategory.setText(R.string.folder_all);
                            if (isShowCamera) {
                                imageAdapter.setShowCamera(true);
                            } else {
                                imageAdapter.setShowCamera(false);
                            }
                        } else {
                            Folder folder = (Folder) v.getAdapter().getItem(index);
                            if (null != folder) {
                                imageAdapter.setData(folder.images);
                                tvCategory.setText(folder.name);
                                // 设定默认选择
                                if (resultList != null && resultList.size() > 0) {
                                    imageAdapter.setDefaultSelected(resultList);
                                }
                            }
                            imageAdapter.setShowCamera(false);
                        }

                        // 滑动到最初始位置
                        gridView.smoothScrollToPosition(0);
                    }
                }, 100);
            }
        });
    }

    @PermissionSuccess(requestCode = PERMISSIONS_CAMERA)
    public void requestSuccess() {
        showCameraAction();
    }

    @PermissionFail(requestCode = PERMISSIONS_CAMERA)
    public void requestFailure() {
        showToast(R.string.camera_permission_fail);
    }

    /**
     * 选择相机
     */
    private void showCameraAction() {
        // 跳转到系统照相机
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            // 设置系统相机拍照后的输出路径
            // 创建临时文件
            try {
                tmpFile = FileUtils.createTmpFile(this);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (tmpFile != null && tmpFile.exists()) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tmpFile));
                } else {
                    ContentValues contentValues = new ContentValues(1);
                    contentValues.put(MediaStore.Images.Media.DATA, tmpFile.getAbsolutePath());
                    Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                }
                startActivityForResult(cameraIntent, REQUEST_CAMERA);
            } else {
                showToast(R.string.image_fail);
            }
        } else {
            showToast(R.string.msg_no_camera);
        }
    }

    /**
     * 选择图片操作
     *
     * @param image
     */
    private void selectImageFromGrid(Image image, int mode) {
        if (image != null) {
            // 多选模式
            if (mode == MODE_MULTI) {
                if (resultList.contains(image.path)) {
                    resultList.remove(image.path);
                    if (resultList.size() != 0) {
                        btnPreview.setEnabled(true);
                        btnPreview.setText(getResources().getString(R.string.preview) + "(" + resultList.size() + ")");
                    } else {
                        btnPreview.setEnabled(false);
                        btnPreview.setText(R.string.preview);
                    }

                    btnSubmit.setText(String.format("%s(%d/%d)", getString(R.string.action_done), resultList.size(), desireImageCount));
                    // 当为选择图片时候的状态
                    if (ListUtils.isEmpty(resultList)) {
                        btnSubmit.setText(R.string.action_done);
                        btnSubmit.setEnabled(false);
                    }
                } else {
                    // 判断选择数量问题
                    if (desireImageCount == resultList.size()) {
                        showToast(R.string.msg_amount_limit);
                        return;
                    }

                    if (!resultList.contains(image.path))
                        resultList.add(image.path);
                    btnPreview.setEnabled(true);
                    btnPreview.setText(getResources().getString(R.string.preview) + "(" + resultList.size() + ")");

                    // 有图片之后，改变按钮状态
                    if (!ListUtils.isEmpty(resultList)) {
                        btnSubmit.setText(String.format("%s(%d/%d)", getString(R.string.action_done), resultList.size(), desireImageCount));
                        if (!btnSubmit.isEnabled()) {
                            btnSubmit.setEnabled(true);
                        }
                    }
                }
                imageAdapter.select(image);
            } else if (mode == MODE_SINGLE) {
                // 单选模式
                // 开始裁剪
                beginCrop(new File(image.path));
            }
        }
    }

    private Folder getFolderByPath(String path) {
        if (resultFolder != null)
            for (Folder folder : resultFolder)
                if (TextUtils.equals(folder.path, path))
                    return folder;
        return null;
    }

    // 开始裁剪（只限于单选）
    private void beginCrop(File sourceFile) {
        UCrop uCrop = UCrop.of(Uri.fromFile(sourceFile), Uri.fromFile(new File(FileManager.getCachePath(), System.currentTimeMillis() + ".0")));
        UCrop.Options options = new UCrop.Options();
        switch (cropType) {
            case CropType.TYPE_FREESTYLE: // 自由选择
                options.setFreeStyleCropEnabled(true);
                break;
            case CropType.TYPE_ORIGIN: // 图片本身尺寸
                uCrop.useSourceImageAspectRatio();
                break;
            case CropType.TYPE_SQUARE: // 正方形
                uCrop.withAspectRatio(ratioX, ratioY);
                break;
        }
        options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
        options.setCompressionQuality(80);
        uCrop.withOptions(options);
        uCrop.start(this);
    }

    // 保存裁剪之后的图片数据
    private void handleCrop(Intent result) {
        Uri resultUri = UCrop.getOutput(result);
        Intent data = new Intent();
        resultList.add(resultUri.getPath());
        data.putStringArrayListExtra(EXTRA_RESULT, resultList);
        setResult(RESULT_OK, data);
        finish();
    }

    private LoaderManager.LoaderCallbacks<Cursor> loaderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {

        private final String[] IMAGE_PROJECTION = {
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Media.MIME_TYPE,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media._ID};

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            if (id == LOADER_ALL) {
                CursorLoader cursorLoader = new CursorLoader(SelectImageAty.this,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION,
                        IMAGE_PROJECTION[4] + ">0 AND " + IMAGE_PROJECTION[3] + "=? OR " + IMAGE_PROJECTION[3] + "=? ",
                        new String[]{"image/jpeg", "image/png"}, IMAGE_PROJECTION[2] + " DESC");
                return cursorLoader;
            } else if (id == LOADER_CATEGORY) {
                CursorLoader cursorLoader = new CursorLoader(SelectImageAty.this,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION,
                        IMAGE_PROJECTION[4] + ">0 AND " + IMAGE_PROJECTION[0] + " like '%" + args.getString("path") + "%'",
                        null, IMAGE_PROJECTION[2] + " DESC");
                return cursorLoader;
            }

            return null;
        }

        private boolean fileExist(String path) {
            if (!TextUtils.isEmpty(path)) {
                return new File(path).exists();
            }
            return false;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (data != null) {
                if (data.getCount() > 0) {
                    List<Image> images = new ArrayList<>();
                    data.moveToFirst();
                    do {
                        String path = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[0]));
                        String name = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[1]));
                        long dateTime = data.getLong(data.getColumnIndexOrThrow(IMAGE_PROJECTION[2]));
                        Image image = null;
                        if (fileExist(path)) {
                            image = new Image(path, name, dateTime);
                            images.add(image);
                        }
                        if (!hasFolderGened) {
                            // 获取文件夹名称
                            File folderFile = new File(path).getParentFile();
                            if (folderFile != null && folderFile.exists()) {
                                String fp = folderFile.getAbsolutePath();
                                Folder f = getFolderByPath(fp);
                                if (f == null) {
                                    Folder folder = new Folder();
                                    folder.name = folderFile.getName();
                                    folder.path = fp;
                                    folder.cover = image;
                                    List<Image> imageList = new ArrayList<>();
                                    imageList.add(image);
                                    folder.images = imageList;
                                    resultFolder.add(folder);
                                } else {
                                    f.images.add(image);
                                }
                            }
                        }

                    } while (data.moveToNext());

                    imageAdapter.setData(images);
                    // 设定默认选择
                    if (resultList != null && resultList.size() > 0) {
                        imageAdapter.setDefaultSelected(resultList);
                    }

                    if (!hasFolderGened) {
                        folderAdapter.setData(resultFolder);
                        hasFolderGened = true;
                    }
                }
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
        }
    };
}
