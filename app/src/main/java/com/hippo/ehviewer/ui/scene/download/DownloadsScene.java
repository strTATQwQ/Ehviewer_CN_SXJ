/*
 * Copyright 2016 Hippo Seven
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hippo.ehviewer.ui.scene.download;

import static com.hippo.ehviewer.ui.scene.download.part.DownloadAdapter.DRAG_ENABLE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.h6ah4i.android.widget.advrecyclerview.animator.DraggableItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.hippo.android.resource.AttrResources;
import com.hippo.drawable.AddDeleteDrawable;
import com.hippo.drawerlayout.DrawerLayout;
import com.hippo.easyrecyclerview.EasyRecyclerView;
import com.hippo.easyrecyclerview.FastScroller;
import com.hippo.easyrecyclerview.HandlerDrawable;
import com.hippo.easyrecyclerview.MarginItemDecoration;
import com.hippo.ehviewer.Analytics;
import com.hippo.ehviewer.EhApplication;
import com.hippo.ehviewer.R;
import com.hippo.ehviewer.Settings;
import com.hippo.ehviewer.callBack.DownloadSearchCallback;
import com.hippo.ehviewer.client.EhConfig;
import com.hippo.ehviewer.client.EhUtils;
import com.hippo.ehviewer.dao.DownloadInfo;
import com.hippo.ehviewer.download.DownloadManager;
import com.hippo.ehviewer.download.DownloadService;
import com.hippo.ehviewer.event.SomethingNeedRefresh;
import com.hippo.ehviewer.spider.SpiderInfo;
import com.hippo.ehviewer.ui.GalleryActivity;
import com.hippo.ehviewer.ui.MainActivity;
import com.hippo.ehviewer.ui.scene.ToolbarScene;
import com.hippo.ehviewer.ui.scene.download.part.DownloadAdapter;
import com.hippo.ehviewer.ui.scene.download.part.DownloadAlbumImporter;
import com.hippo.ehviewer.ui.scene.download.part.DownloadArchiveImporter;
import com.hippo.ehviewer.ui.scene.download.part.DownloadBatchActions;
import com.hippo.ehviewer.ui.scene.download.part.DownloadChoiceListener;
import com.hippo.ehviewer.ui.scene.download.part.DownloadGuideHelper;
import com.hippo.ehviewer.ui.scene.download.part.DownloadPaginationController;
import com.hippo.ehviewer.ui.scene.download.part.DownloadSearchController;
import com.hippo.ehviewer.widget.MyEasyRecyclerView;
import com.hippo.ehviewer.widget.SearchBar;
import com.hippo.lib.yorozuya.AssertUtils;
import com.hippo.lib.yorozuya.ObjectUtils;
import com.hippo.lib.yorozuya.ViewUtils;
import com.hippo.ripple.Ripple;
import com.hippo.util.DrawableManager;
import com.hippo.view.ViewTransition;
import com.hippo.widget.FabLayout;
import com.hippo.widget.ProgressView;
import com.hippo.widget.SearchBarMover;
import com.hippo.widget.recyclerview.AutoStaggeredGridLayoutManager;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static com.hippo.ehviewer.spider.SpiderDen.getExistingGalleryDownloadDir;
import static com.hippo.ehviewer.spider.SpiderDen.getGalleryDownloadDir;
import static com.hippo.ehviewer.spider.SpiderDen.findImageFile;
import static com.hippo.ehviewer.spider.SpiderInfo.getSpiderInfo;
import static com.hippo.util.FileUtils.getFileName;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Point;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.Display;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import androidx.activity.result.ActivityResult;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.SimpleShowcaseEventListener;
import com.github.amlcurran.showcaseview.targets.PointTarget;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.hippo.app.CheckBoxDialogBuilder;
import com.hippo.ehviewer.EhDB;
import com.hippo.ehviewer.client.data.GalleryInfo;
import com.hippo.ehviewer.dao.DownloadLabel;
import com.hippo.ehviewer.sync.DownloadListInfosExecutor;
import com.hippo.ehviewer.sync.DownloadSpiderInfoExecutor;
import com.hippo.ehviewer.sync.nas.NasCatalogClient;
import com.hippo.ehviewer.sync.nas.NasCatalogEntry;
import com.hippo.ehviewer.sync.nas.NasCatalogStore;
import com.hippo.ehviewer.sync.nas.NasConfigStore;
import com.hippo.ehviewer.sync.nas.NasSyncConfig;
import com.hippo.ehviewer.sync.nas.NasSyncEngine;
import com.hippo.ehviewer.sync.nas.NasSyncService;
import com.hippo.ehviewer.ui.annotation.ViewLifeCircle;
import com.hippo.ehviewer.ui.scene.gallery.detail.GalleryDetailScene;
import com.hippo.ehviewer.ui.scene.gallery.list.EnterGalleryDetailTransaction;
import com.hippo.ehviewer.widget.MinimalPaginationView;
import com.hippo.lib.yorozuya.collect.LongList;
import com.hippo.scene.Announcer;
import com.hippo.unifile.UniFile;
import com.hippo.util.ExceptionUtils;
import com.hippo.util.IoThreadPoolExecutor;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

public class DownloadsScene extends ToolbarScene
        implements DownloadManager.DownloadInfoListener, DownloadSearchCallback,
        MyEasyRecyclerView.OnItemClickListener,
        MyEasyRecyclerView.OnItemLongClickListener,
        FabLayout.OnClickFabListener, FabLayout.OnExpandListener, FastScroller.OnDragHandlerListener,
        SearchBar.Helper, SearchBarMover.Helper, SearchBar.OnStateChangeListener,
        DownloadAdapter.DownloadAdapterCallback,
        DownloadArchiveImporter.Host, DownloadAlbumImporter.Host, DownloadSearchController.Host,
        DownloadBatchActions.Host, DownloadPaginationController.Host,
        DownloadGuideHelper.Host, DownloadChoiceListener.Host {

    private static final String TAG = DownloadsScene.class.getSimpleName();

    public static final String KEY_GID = "gid";

    public static final String KEY_ACTION = "action";
    private static final String KEY_LABEL = "label";

    public static final String ACTION_CLEAR_DOWNLOAD_SERVICE = "clear_download_service";

    public static final int LOCAL_GALLERY_INFO_CHANGE = 909;

    private static final long ANIMATE_TIME = 300L;

    @Nullable
    private AddDeleteDrawable mActionFabDrawable;

    /*---------------
         Whole life cycle
         ---------------*/
    @Nullable
    private DownloadManager mDownloadManager;
    @Nullable
    public String mLabel;
    @Nullable
    private List<DownloadInfo> mList;
    @Nullable
    private List<DownloadInfo> mBackList;

    private final Map<Long, NasCatalogEntry> mNasCatalogMap = new LinkedHashMap<>();
    private final Map<Long, Boolean> mLocalAvailability = new HashMap<>();

    private final DownloadArchiveImporter mArchiveImporter = new DownloadArchiveImporter(this);
    private final DownloadAlbumImporter mAlbumImporter = new DownloadAlbumImporter(this);
    private final DownloadSearchController mSearchController = new DownloadSearchController(this);
    private final DownloadBatchActions mBatchActions = new DownloadBatchActions(this);
    private final DownloadPaginationController mPaginationController = new DownloadPaginationController(this);
    private final DownloadGuideHelper mGuideHelper = new DownloadGuideHelper(this);

    /*---------------
     View life cycle
     ---------------*/
    @Nullable
    private MyEasyRecyclerView mRecyclerView;
    @Nullable
    private ViewTransition mViewTransition;
    @Nullable
    private FabLayout mFabLayout;
    @Nullable
    private RecyclerView.Adapter mAdapter;
    @Nullable
    private DownloadAdapter mOriginalAdapter;
    @Nullable
    private AutoStaggeredGridLayoutManager mLayoutManager;

    // 拖拽管理器
    @Nullable
    private RecyclerViewDragDropManager mDragDropManager;

    private ProgressView mProgressView;

    private DownloadLabelDraw downloadLabelDraw;
    public String searchKey = null;

    private int mInitPosition = -1;

    public boolean searching = false;

    @Nullable
    private Spinner mCategorySpinner;
    private int mSelectedCategory = EhUtils.ALL_CATEGORY;

    @NonNull
    private final ActivityResultLauncher<Intent> galleryActivityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            mPaginationController::updateReadProcess
    );

    @NonNull
    private final ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            mArchiveImporter::handleSelectedFile
    );

    @NonNull
    private final ActivityResultLauncher<Intent> folderPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            mAlbumImporter::handleSelectedFolder
    );

    @Override
    public int getNavCheckedItem() {
        return R.id.nav_downloads;
    }

    private boolean handleArguments(Bundle args) {
        if (null == args) {
            return false;
        }

        if (ACTION_CLEAR_DOWNLOAD_SERVICE.equals(args.getString(KEY_ACTION))) {
            DownloadService.Companion.clear();
        }

        long gid;
        if (null != mDownloadManager && -1L != (gid = args.getLong(KEY_GID, -1L))) {
            DownloadInfo info = mDownloadManager.getDownloadInfo(gid);
            if (null != info) {
                mLabel = info.getLabel();
                updateForLabel();
                updateView();

                // Get position
                if (null != mList) {
                    int position = mList.indexOf(info);
                    if (position >= 0 && null != mRecyclerView) {
                        mPaginationController.initPage(position);
                    } else {
                        mInitPosition = position;
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public void onNewArguments(@NonNull Bundle args) {
        handleArguments(args);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context context = getEHContext();
        AssertUtils.assertNotNull(context);
        mDownloadManager = EhApplication.getDownloadManager(context);
        mDownloadManager.addDownloadInfoListener(this);
        mPaginationController.setCanPagination(Settings.getDownloadPagination());
        if (savedInstanceState == null) {
            onInit();
        } else {
            onRestore(savedInstanceState);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mList = null;

        DownloadManager manager = mDownloadManager;
        if (null == manager) {
            Context context = getEHContext();
            if (null != context) {
                manager = EhApplication.getDownloadManager(context);
            }
        } else {
            mDownloadManager = null;
        }

        if (null != manager) {
            manager.removeDownloadInfoListener(this);
        } else {
            Log.e(TAG, "Can't removeDownloadInfoListener");
        }
        mActionFabDrawable = null;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateForLabel() {
        if (null == mDownloadManager) {
            return;
        }

        mLocalAvailability.clear();
        if (mLabel == null) {
            List<DownloadInfo> localItems = mDownloadManager.getDefaultDownloadInfoList();
            refreshNasCatalog();

            Map<Long, DownloadInfo> localByGid = new HashMap<>();
            for (DownloadInfo info : localItems) localByGid.put(info.gid, info);
            List<NasCatalogEntry> nasItems = new ArrayList<>(mNasCatalogMap.values());
            Collections.reverse(nasItems);
            mList = new ArrayList<>(localItems.size() + nasItems.size());
            Set<Long> added = new HashSet<>();
            for (NasCatalogEntry entry : nasItems) {
                if (!entry.label.isEmpty()) continue;
                DownloadInfo local = localByGid.get(entry.gid);
                if (local != null) {
                    mList.add(local);
                    added.add(entry.gid);
                } else if (mDownloadManager.getDownloadInfo(entry.gid) == null) {
                    mList.add(NasCatalogStore.asNasOnlyDownload(entry));
                    added.add(entry.gid);
                }
            }
            for (DownloadInfo local : localItems) {
                if (!added.contains(local.gid)) mList.add(local);
            }
        } else {
            List<DownloadInfo> localLabelItems = mDownloadManager.getLabelDownloadInfoList(mLabel);
            if (localLabelItems == null) {
                mLabel = null;
                mList = mDownloadManager.getDefaultDownloadInfoList();
            } else {
                refreshNasCatalog();
                mList = new ArrayList<>(localLabelItems);
                Set<Long> localGids = new HashSet<>();
                for (DownloadInfo info : mDownloadManager.getAllDownloadInfoList()) {
                    localGids.add(info.gid);
                }
                List<NasCatalogEntry> nasItems = new ArrayList<>(mNasCatalogMap.values());
                Collections.reverse(nasItems);
                for (NasCatalogEntry entry : nasItems) {
                    if (mLabel.equals(entry.label) && !localGids.contains(entry.gid)) {
                        mList.add(0, NasCatalogStore.asNasOnlyDownload(entry));
                    }
                }
            }
        }

        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
        mBackList = mList;
//        filterByCategory();
        updateTitle();
        mPaginationController.updatePaginationIndicator();
        Settings.putRecentDownloadLabel(mLabel);
        mPaginationController.queryUnreadSpiderInfo();
    }

    @SuppressLint("StringFormatMatches")
    private void updateTitle() {
        try {
            setTitle(getString(R.string.scene_download_title_new,
                    mLabel != null ? mLabel : getString(R.string.default_download_label_name),
                    Integer.toString(mList == null ? 0 : mList.size())));
        } catch (Exception e) {
            Analytics.recordException(e);
            setTitle(getString(R.string.scene_download_title_new,
                    mLabel != null ? mLabel : getString(R.string.default_download_label_name)));
        }
    }

    private void onInit() {
        if (!handleArguments(getArguments())) {
            mLabel = Settings.getRecentDownloadLabel();
            updateForLabel();
        }
    }

    private void onRestore(@NonNull Bundle savedInstanceState) {
        mLabel = savedInstanceState.getString(KEY_LABEL);
        updateForLabel();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_LABEL, mLabel);
    }

    @Nullable
    @Override
    public View onCreateView3(LayoutInflater inflater,
                              @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.scene_download, container, false);

        Context context = getEHContext();
        assert context != null;

        setupCategorySpinner(view, context);

        mProgressView = (ProgressView) ViewUtils.$$(view, R.id.download_progress_view);
        View content = ViewUtils.$$(view, R.id.content);
        mRecyclerView = (MyEasyRecyclerView) ViewUtils.$$(content, R.id.recycler_view);
        FastScroller fastScroller = (FastScroller) ViewUtils.$$(content, R.id.fast_scroller);
        mFabLayout = (FabLayout) ViewUtils.$$(view, R.id.fab_layout);
        TextView tip = (TextView) ViewUtils.$$(view, R.id.tip);
        MinimalPaginationView paginationIndicator = view.findViewById(R.id.indicator);
        mPaginationController.setPaginationIndicator(paginationIndicator);
        paginationIndicator.setOnInteractionStateChangeListener(this::setPaginationDrawerLock);

        mViewTransition = new ViewTransition(content, tip);

        Resources resources = context.getResources();

        Drawable drawable = DrawableManager.getVectorDrawable(context, R.drawable.big_download);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        tip.setCompoundDrawables(null, drawable, null, null);

        setupRecyclerView(context, resources);
        setupFastScroller(fastScroller, context);
        setupFabLayout(context, resources);

        updateView();

        mGuideHelper.guide();
        mPaginationController.updatePaginationIndicator();
        return view;
    }

    private void setupCategorySpinner(@NonNull View view, @NonNull Context context) {
        mCategorySpinner = (Spinner) ViewUtils.$$(view, R.id.category_spinner);
        // Initialize category spinner
        List<String> categoryList = new ArrayList<>();
        categoryList.add(getString(R.string.category_all)); // Add "All" option
        categoryList.add(Objects.requireNonNull(EhUtils.getCategory(EhConfig.DOUJINSHI)).toUpperCase(Locale.ROOT));
        categoryList.add(Objects.requireNonNull(EhUtils.getCategory(EhConfig.MANGA)).toUpperCase(Locale.ROOT));
        categoryList.add(Objects.requireNonNull(EhUtils.getCategory(EhConfig.ARTIST_CG)).toUpperCase(Locale.ROOT));
        categoryList.add(Objects.requireNonNull(EhUtils.getCategory(EhConfig.GAME_CG)).toUpperCase(Locale.ROOT));
        categoryList.add(Objects.requireNonNull(EhUtils.getCategory(EhConfig.WESTERN)).toUpperCase(Locale.ROOT));
        categoryList.add(Objects.requireNonNull(EhUtils.getCategory(EhConfig.NON_H)).toUpperCase(Locale.ROOT));
        categoryList.add(Objects.requireNonNull(EhUtils.getCategory(EhConfig.IMAGE_SET)).toUpperCase(Locale.ROOT));
        categoryList.add(Objects.requireNonNull(EhUtils.getCategory(EhConfig.COSPLAY)).toUpperCase(Locale.ROOT));
        categoryList.add(Objects.requireNonNull(EhUtils.getCategory(EhConfig.ASIAN_PORN)).toUpperCase(Locale.ROOT));
        categoryList.add(Objects.requireNonNull(EhUtils.getCategory(EhConfig.MISC)).toUpperCase(Locale.ROOT));
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, categoryList);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCategorySpinner.setAdapter(categoryAdapter);
        mCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int selectedCategory;
                switch (position) {
                    case 0:
                        selectedCategory = EhUtils.ALL_CATEGORY;
                        break;
                    case 1:
                        selectedCategory = EhConfig.DOUJINSHI;
                        break;
                    case 2:
                        selectedCategory = EhConfig.MANGA;
                        break;
                    case 3:
                        selectedCategory = EhConfig.ARTIST_CG;
                        break;
                    case 4:
                        selectedCategory = EhConfig.GAME_CG;
                        break;
                    case 5:
                        selectedCategory = EhConfig.WESTERN;
                        break;
                    case 6:
                        selectedCategory = EhConfig.NON_H;
                        break;
                    case 7:
                        selectedCategory = EhConfig.IMAGE_SET;
                        break;
                    case 8:
                        selectedCategory = EhConfig.COSPLAY;
                        break;
                    case 9:
                        selectedCategory = EhConfig.ASIAN_PORN;
                        break;
                    case 10:
                        selectedCategory = EhConfig.MISC;
                        break;
                    default:
                        selectedCategory = EhUtils.ALL_CATEGORY;
                        break;
                }
                if (selectedCategory != mSelectedCategory) {
                    mSelectedCategory = selectedCategory;
                    filterByCategory();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
        // Set default selection
        mCategorySpinner.setSelection(0);
    }

    private void setupRecyclerView(@NonNull Context context, @NonNull Resources resources) {
        // 初始化拖拽管理器
        mDragDropManager = new RecyclerViewDragDropManager();
        try {
            mDragDropManager.setDraggingItemShadowDrawable(
                    (NinePatchDrawable) context.getResources().getDrawable(R.drawable.shadow_8dp));
        } catch (Exception e) {
            // 忽略硬件位图相关错误
            android.util.Log.w("DownloadsScene", "Error setting drag shadow: " + e.getMessage());
        }


        mOriginalAdapter = new DownloadAdapter(this, this);
        mOriginalAdapter.setHasStableIds(true);
        mAdapter = mDragDropManager.createWrappedAdapter(mOriginalAdapter); // 包装适配器以支持拖拽
        mDragDropManager.setCheckCanDropEnabled(false);
        mRecyclerView.setAdapter(mAdapter);

        // 初始化分页监听器
        mPaginationController.bindPageChangeListener(mOriginalAdapter, mRecyclerView);
        mLayoutManager = new AutoStaggeredGridLayoutManager(0, StaggeredGridLayoutManager.VERTICAL);
        mLayoutManager.setColumnSize(resources.getDimensionPixelOffset(Settings.getDetailSizeResId()));
        mLayoutManager.setStrategy(AutoStaggeredGridLayoutManager.STRATEGY_MIN_SIZE);

        // 设置拖拽动画器
        final GeneralItemAnimator animator = new DraggableItemAnimator();
        mRecyclerView.setItemAnimator(animator);

        mRecyclerView.setItemViewCacheSize(100);
        try {
            mRecyclerView.setDrawingCacheEnabled(true);
            mRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        } catch (Exception e) {
            // 忽略硬件位图相关错误
            android.util.Log.w("DownloadsScene", "Error setting drawing cache: " + e.getMessage());
        }
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setSelector(Ripple.generateRippleDrawable(context, !AttrResources.getAttrBoolean(context, androidx.appcompat.R.attr.isLightTheme), new ColorDrawable(Color.TRANSPARENT)));
        mRecyclerView.setDrawSelectorOnTop(true);
        mRecyclerView.setClipToPadding(false);
        mRecyclerView.setOnItemClickListener(this);
        mRecyclerView.setOnItemLongClickListener(this);
        mRecyclerView.setChoiceMode(MyEasyRecyclerView.CHOICE_MODE_MULTIPLE_CUSTOM);
        mRecyclerView.setCustomCheckedListener(new DownloadChoiceListener(this));
//        mRecyclerView.setOnGenericMotionListener(this::onGenericMotion);
        // Cancel change animation
        RecyclerView.ItemAnimator itemAnimator = mRecyclerView.getItemAnimator();
        if (itemAnimator instanceof GeneralItemAnimator) {
            ((GeneralItemAnimator) itemAnimator).setSupportsChangeAnimations(false);
        }
        int interval = resources.getDimensionPixelOffset(R.dimen.gallery_list_interval);
        int paddingH = resources.getDimensionPixelOffset(R.dimen.gallery_list_margin_h);
        int paddingV = resources.getDimensionPixelOffset(R.dimen.gallery_list_margin_v);
        MarginItemDecoration decoration = new MarginItemDecoration(interval, paddingH, paddingV, paddingH, paddingV);
        mRecyclerView.addItemDecoration(decoration);
        decoration.applyPaddings(mRecyclerView);

        // 将拖拽管理器附加到RecyclerView
        if (mDragDropManager != null) {
            try {
                mDragDropManager.attachRecyclerView(mRecyclerView);
            } catch (Exception e) {
                // 忽略硬件位图相关错误
                android.util.Log.w("DownloadsScene", "Error attaching drag manager: " + e.getMessage());
            }
        }

        if (mInitPosition >= 0 && mPaginationController.indexPage != 1) {
            mPaginationController.initPage(mInitPosition);
            mRecyclerView.scrollToPosition(listIndexInPage(mInitPosition));
            mInitPosition = -1;
        }
    }

    private void setupFastScroller(@NonNull FastScroller fastScroller, @NonNull Context context) {
        fastScroller.attachToRecyclerView(mRecyclerView);
        HandlerDrawable handlerDrawable = new HandlerDrawable();
        handlerDrawable.setColor(AttrResources.getAttrColor(context, R.attr.widgetColorThemeAccent));
        fastScroller.setHandlerDrawable(handlerDrawable);
        fastScroller.setOnDragHandlerListener(this);
    }

    private void setupFabLayout(@NonNull Context context, @NonNull Resources resources) {
        mFabLayout.setExpanded(false, true);
        mFabLayout.setHidePrimaryFab(false);
        mFabLayout.setAutoCancel(false);
        mFabLayout.setOnClickFabListener(this);
        mFabLayout.setOnExpandListener(this);
        mActionFabDrawable = new AddDeleteDrawable(context, resources.getColor(R.color.primary_drawable_dark, null));
        mFabLayout.getPrimaryFab().setImageDrawable(mActionFabDrawable);
        FloatingActionButton fab = mFabLayout.getSecondaryFabAt(6);
        if (DRAG_ENABLE) {
            fab.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.v_mobile_hand_left_x24, context.getTheme()));
        } else {
            fab.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.v_mobile_hand_left_off_x24, context.getTheme()));
        }
        addAboveSnackView(mFabLayout);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateTitle();
        setNavigationIcon(R.drawable.v_arrow_left_dark_x24);
    }

    @Override
    public void onDestroyView() {
        MinimalPaginationView pager = mPaginationController.getPaginationIndicator();
        if (pager != null) {
            pager.dismissFastPanel();
            pager.setOnInteractionStateChangeListener(null);
        }
        setPaginationDrawerLock(false);
        mPaginationController.setPaginationIndicator(null);
        super.onDestroyView();

        mGuideHelper.destroy();
        if (null != mRecyclerView) {
            mRecyclerView.stopScroll();
            mRecyclerView = null;
        }
        if (null != mFabLayout) {
            removeAboveSnackView(mFabLayout);
            mFabLayout = null;
        }

        mRecyclerView = null;
        mViewTransition = null;
        mAdapter = null;
        mOriginalAdapter = null;
        mLayoutManager = null;
        mDragDropManager = null;
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onNavigationClick(View view) {
        onBackPressed();
    }

    @Override
    public int getMenuResId() {
        return R.menu.scene_download;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        // Skip when in choice mode
        Activity activity = getActivity2();
        if (null == activity || null == mRecyclerView || mRecyclerView.isInCustomChoice()) {
            return false;
        }

        int id = item.getItemId();
        switch (id) {
            case R.id.action_nas_upload_all:
                confirmBulkNasSync(NasSyncEngine.Direction.UPLOAD_TO_NAS);
                return true;
            case R.id.action_nas_upload_metadata:
                confirmBulkNasSync(NasSyncEngine.Direction.UPLOAD_DATABASE_TO_NAS);
                return true;
            case R.id.action_nas_download_all:
                confirmBulkNasSync(NasSyncEngine.Direction.DOWNLOAD_TO_PHONE);
                return true;
            case R.id.action_nas_download_metadata: {
                Context context = getEHContext();
                if (context == null) return false;
                NasSyncService.start(context, NasSyncService.ACTION_IMPORT_DATABASE);
                Toast.makeText(context, R.string.nas_sync_started_background,
                        Toast.LENGTH_SHORT).show();
                return true;
            }
            case R.id.action_start_all: {
                Intent intent = new Intent(activity, DownloadService.class);
                intent.setAction(DownloadService.ACTION_START_ALL);
                activity.startService(intent);
                return true;
            }
            case R.id.action_stop_all: {
                if (null != mDownloadManager) {
                    mDownloadManager.stopAllDownload();
                }
                return true;
            }
            case R.id.action_reset_reading_progress: {
                Context context = getEHContext();
                    if (context == null) {
                    return false;
                }
                if (searching) {
                    Toast.makeText(context, R.string.download_searching, Toast.LENGTH_LONG).show();
                    return true;
                }
                new AlertDialog.Builder(context)
                        .setMessage(R.string.reset_reading_progress_message)
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                            mPaginationController.resetReadingProgressInUi();
                            if (mDownloadManager != null) {
                                Toast.makeText(context, R.string.reset_reading_progress_processing,
                                        Toast.LENGTH_SHORT).show();
                                mDownloadManager.resetAllReadingProgress(() ->
                                        Toast.makeText(context, R.string.reset_reading_progress_done,
                                                Toast.LENGTH_SHORT).show());
                            }
                        }).show();
                return true;
            }
            case R.id.search_download_gallery: {
                Context context = getEHContext();
                if (context == null) {
                    return false;
                }
                mSearchController.gotoSearch(context, this, this);
                return true;
            }
            case R.id.all:
            case R.id.sort_by_default:
            case R.id.download_done:
            case R.id.not_started:
            case R.id.waiting:
            case R.id.downloading:
            case R.id.failed:
            case R.id.sort_by_gallery_id_asc:
            case R.id.sort_by_gallery_id_desc:
            case R.id.sort_by_create_time_asc:
            case R.id.sort_by_create_time_desc:
            case R.id.sort_by_rating_asc:
            case R.id.sort_by_rating_desc:
            case R.id.sort_by_name_asc:
            case R.id.sort_by_name_desc:
            case R.id.sort_by_file_size_asc:
            case R.id.sort_by_file_size_desc:
            case R.id.all_kind:
            case R.id.misc:
            case R.id.doujinshi:
            case R.id.manga:
            case R.id.artist_cg:
            case R.id.game_cg:
            case R.id.image_set:
            case R.id.cosplay:
            case R.id.asian_porn:
            case R.id.non_h:
            case R.id.western:
            case R.id.unknown:
            case R.id.local_import:
                mSearchController.gotoFilterAndSort(id);
                return true;
            case R.id.import_local_archive:
                mArchiveImporter.importLocalArchive(filePickerLauncher);
                return true;
            case R.id.import_local_album:
                mAlbumImporter.importLocalAlbum(folderPickerLauncher);
                return true;
//            case R.id.misc:
//            case R.id.doujinshi:
//            case R.id.manga:
//            case R.id.artist_cg:
//            case R.id.game_cg:
//            case R.id.image_set:
//            case R.id.cosplay:
//            case R.id.asian_porn:
//            case R.id.non_h:
//            case R.id.western:
//            case R.id.unknown:
//
//                return true;
        }
        return false;
    }

    public void updateView() {
        if (mViewTransition != null) {
            if (mList == null || mList.size() == 0) {
                mViewTransition.showView(1);
            } else {
                mViewTransition.showView(0);
            }
        }
    }

    @Override
    public View onCreateDrawerView(LayoutInflater inflater,
                                   @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (downloadLabelDraw == null) {
            downloadLabelDraw = new DownloadLabelDraw(inflater, container, this);
        }

        return downloadLabelDraw.createView();
    }

    @Override
    public void onBackPressed() {
        if (mGuideHelper.isShowing()) {
            return;
        }

        if (mRecyclerView != null && mRecyclerView.isInCustomChoice()) {
            mRecyclerView.outOfCustomChoiceMode();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onStartDragHandler() {
        // Lock right drawer
        setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.RIGHT);
    }

    @Override
    public void onEndDragHandler() {
        // Restore right drawer
        if (null != mRecyclerView && !mRecyclerView.isInCustomChoice()) {
            setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, Gravity.RIGHT);
        }
    }

    @Override
    public boolean onItemClick(EasyRecyclerView parent, View view, int position, long id) {
        Activity activity = getActivity2();
        MyEasyRecyclerView recyclerView = mRecyclerView;
        if (null == activity || null == recyclerView) {
            return false;
        }

        if (recyclerView.isInCustomChoice()) {
            recyclerView.toggleItemChecked(position);
            return true;
        } else {
            List<DownloadInfo> list = mList;
            if (list == null) {
                return false;
            }
            int listPosition = positionInList(position);
            if (position < 0 || listPosition < 0 || listPosition >= list.size()) {
                return false;
            }

            return readDownload(list.get(listPosition));
        }
    }

    @Override
    public boolean onItemLongClick(EasyRecyclerView parent, View view, int position, long id) {
        MyEasyRecyclerView recyclerView = mRecyclerView;
        if (recyclerView == null) {
            return false;
        }

        if (!recyclerView.isInCustomChoice()) {
            recyclerView.intoCustomChoiceMode();
        }
        recyclerView.toggleItemChecked(position);

        return true;
    }

    @SuppressLint("RtlHardcoded")
    @Override
    public void onExpand(boolean expanded) {
        if (null == mActionFabDrawable) {
            return;
        }

        if (expanded) {
            setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.LEFT);
            setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.RIGHT);
            mActionFabDrawable.setDelete(ANIMATE_TIME);
        } else {
            setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, Gravity.LEFT);
            setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, Gravity.RIGHT);
            mActionFabDrawable.setAdd(ANIMATE_TIME);
        }
    }

    @Override
    public void onClickPrimaryFab(FabLayout view, FloatingActionButton fab) {
        if (mRecyclerView != null && mRecyclerView.isInCustomChoice()) {
            mRecyclerView.outOfCustomChoiceMode();
            return;
        }
        if (mRecyclerView != null && !mRecyclerView.isInCustomChoice()) {
            mRecyclerView.intoCustomChoiceMode();
            return;
        }
        view.toggle();
    }

    @Override
    public void onClickSecondaryFab(FabLayout view, FloatingActionButton fab, int position) {
        boolean hasNas = false;
        if (mRecyclerView != null && mList != null) {
            SparseBooleanArray checked = mRecyclerView.getCheckedItemPositions();
            for (int i = 0; i < checked.size(); i++) {
                int index = positionInList(checked.keyAt(i));
                if (checked.valueAt(i) && index >= 0 && index < mList.size()
                        && mNasCatalogMap.containsKey(mList.get(index).gid)) hasNas = true;
            }
        }
        if (hasNas) onClickNasSecondaryFab(view, fab, position);
        else mBatchActions.onClickSecondaryFab(view, fab, position);
    }

    @Override
    public void onAdd(@NonNull DownloadInfo info, @NonNull List<DownloadInfo> list, int position) {
        if (mLabel == null && mList != list) {
            updateForLabel();
            updateView();
            return;
        }
        if (mList != list) {
            return;
        }
        if (mAdapter != null) {
            mAdapter.notifyItemInserted(position);
        }
        if (downloadLabelDraw != null) {
            downloadLabelDraw.updateDownloadLabels();
        }
        updateView();
    }

    @Override
    public void onReplace(@NonNull DownloadInfo newInfo, @NonNull DownloadInfo oldInfo) {
        if (mList == null) {
            return;
        }
        updateForLabel();
        updateView();

        int index = mList.indexOf(newInfo);
        if (index >= 0 && mAdapter != null) {
//            mSpiderInfoMap.put(info.gid,getSpiderInfo(info));
            mAdapter.notifyItemChanged(listIndexInPage(index));
        }
        List<DownloadInfo> infos = new ArrayList<>();
        infos.add(newInfo);
        mPaginationController.fetchSpiderInfo(infos);
    }

    @Override
    public void onUpdate(@NonNull DownloadInfo info, @NonNull List<DownloadInfo> list, LinkedList<DownloadInfo> mWaitList) {
        if (mList != list && !mList.contains(info)) {
            return;
        }
        int index = mList.indexOf(info);
        if (index >= 0 && mAdapter != null) {
            mAdapter.notifyItemChanged(listIndexInPage(index));
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onUpdateAll() {
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onReload() {
        if (mLabel == null) {
            updateForLabel();
        }
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
        if (downloadLabelDraw != null) downloadLabelDraw.updateDownloadLabels();
        updateView();
    }

    @Override
    public void onChange() {
        mLabel = null;
        updateForLabel();
        updateView();
    }

    @Override
    public void onRenameLabel(String from, String to) {
        if (!ObjectUtils.equal(mLabel, from)) {
            return;
        }

        mLabel = to;
        updateForLabel();
        updateView();
    }

    @Override
    public void onRemove(@NonNull DownloadInfo info, @NonNull List<DownloadInfo> list, int position) {
        if (mLabel == null && mList != list) {
            updateForLabel();
            updateView();
            return;
        }
        if (mList != list) {
            return;
        }
        if (mAdapter != null) {
            mAdapter.notifyItemRemoved(listIndexInPage(position));
        }
        if (downloadLabelDraw != null) downloadLabelDraw.updateDownloadLabels();
        updateView();
    }

    @Override
    public void onUpdateLabels() {
        // TODO
    }

    @Nullable
    public DownloadManager getMDownloadManager() {
        return mDownloadManager;
    }

    // DownloadAdapterCallback 接口实现
    @Override
    public int getIndexPage() {
        return mPaginationController.indexPage;
    }

    @Override
    public int getPageSize() {
        return mPaginationController.pageSize;
    }

    @Override
    public int getPaginationSize() {
        return mPaginationController.paginationSize;
    }

    @Override
    public boolean isCanPagination() {
        return mPaginationController.isCanPagination();
    }

    @Override
    public int positionInList(int position) {
        return mPaginationController.positionInList(position);
    }

    @Override
    public int listIndexInPage(int position) {
        return mPaginationController.listIndexInPage(position);
    }

    @Override
    public List<DownloadInfo> getList() {
        return mList;
    }

    @Override
    public Map<Long, SpiderInfo> getSpiderInfoMap() {
        return mPaginationController.getSpiderInfoMap();
    }

    @Override
    public DownloadManager getDownloadManager() {
        return mDownloadManager;
    }

    @Nullable
    @Override
    public String getLabel() {
        return mLabel;
    }

    @Override
    public MyEasyRecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    @Override
    public RecyclerView.Adapter getNotifyAdapter() {
        return mAdapter;
    }

    @Nullable
    @Override
    public FabLayout getFabLayout() {
        return mFabLayout;
    }

    @Nullable
    @Override
    public AutoStaggeredGridLayoutManager getLayoutManager() {
        return mLayoutManager;
    }

    @Override
    public ProgressView getProgressView() {
        return mProgressView;
    }

    @Override
    public List<DownloadInfo> getBackList() {
        return mBackList;
    }

    @Override
    public DownloadSearchCallback getDownloadSearchCallback() {
        return this;
    }

    @Override
    public String getSearchKey() {
        return searchKey;
    }

    @Override
    public void setSearchKey(String searchKey) {
        this.searchKey = searchKey;
    }

    @Override
    public void setSearching(boolean searching) {
        this.searching = searching;
    }

    @Override
    public MyEasyRecyclerView.OnItemLongClickListener getItemLongClickListener() {
        return this;
    }

    @Override
    public void launchGalleryActivity(Intent intent) {
        galleryActivityLauncher.launch(intent);
    }

    public void runOnUiThread(Runnable runnable) {
        Activity activity = getActivity2();
        if (activity != null) {
            activity.runOnUiThread(runnable);
        }
    }

    @Override
    public void onClickTitle() {
        mSearchController.onClickTitle();
    }

    @Override
    public void onClickLeftIcon() {
        mSearchController.onClickLeftIcon();
    }

    @Override
    public void onClickRightIcon() {
        mSearchController.onClickRightIcon();
    }

    @Override
    public void onSearchEditTextClick() {
        mSearchController.onSearchEditTextClick();
    }

    @Override
    public void onApplySearch(String query) {
        mSearchController.onApplySearch(query);
    }

    protected void startSearching() {
        mSearchController.startSearching();
    }

    private void updateAdapter() {
        // 检查 Fragment 是否已附加，如果未附加则延迟创建适配器
        if (!isAdded()) {
            return;
        }
        mOriginalAdapter = new DownloadAdapter(this, this);
        mOriginalAdapter.setHasStableIds(true);
        // 避免重复创建包装适配器，直接使用原始适配器
        mAdapter = mOriginalAdapter;
        if (mRecyclerView != null) {
            mRecyclerView.setAdapter(mAdapter);
        }
    }

    @Override
    public void onSearchEditTextBackPressed() {
        mSearchController.onSearchEditTextBackPressed();
    }

    @Override
    public void onStateChange(SearchBar searchBar, int newState, int oldState, boolean animation) {
        mSearchController.onStateChange(searchBar, newState, oldState, animation);
    }

    @Override
    public boolean isValidView(RecyclerView recyclerView) {
        return mSearchController.isValidView(recyclerView);
    }

    @Nullable
    @Override
    public RecyclerView getValidRecyclerView() {
        return mSearchController.getValidRecyclerView();
    }

    @Override
    public boolean forceShowSearchBar() {
        return mSearchController.forceShowSearchBar();
    }

    @Override
    public void onDownloadSearchSuccess(List<DownloadInfo> list) {
        // 检查 Fragment 是否已附加，如果未附加则忽略回调
        if (!isAdded()) {
            return;
        }
        mList = list;
        updateAdapter();
        mProgressView.setVisibility(View.GONE);
        if (mRecyclerView != null) {
            mRecyclerView.setVisibility(View.VISIBLE);
        }
        searching = false;
        mPaginationController.queryUnreadSpiderInfo();
    }

    @Override
    public void onDownloadListHandleSuccess(List<DownloadInfo> list) {
        // 检查 Fragment 是否已附加，如果未附加则忽略回调
        if (!isAdded()) {
            return;
        }
        mList = list;
        updateAdapter();
        mProgressView.setVisibility(View.GONE);
        if (mRecyclerView != null) {
            mRecyclerView.setVisibility(View.VISIBLE);
        }
        mPaginationController.queryUnreadSpiderInfo();
    }

    @Override
    public void onDownloadSearchFailed(List<DownloadInfo> list) {
        Toast.makeText(getEHContext(), R.string.download_searching_failed, Toast.LENGTH_LONG).show();
        mList = list;
        updateAdapter();
        mProgressView.setVisibility(View.GONE);
        if (mRecyclerView != null) {
            mRecyclerView.setVisibility(View.VISIBLE);
        }
        searching = false;
        mPaginationController.queryUnreadSpiderInfo();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateDownloadLabels(SomethingNeedRefresh somethingNeedRefresh) {
        if (somethingNeedRefresh.isDownloadLabelDrawNeed()) {
            downloadLabelDraw.updateDownloadLabels();
        }
    }

    private void filterByCategory() {
        if (mBackList == null) {
            return;
        }
        if (mSelectedCategory == EhUtils.ALL_CATEGORY) {
            mList = new ArrayList<>(mBackList);
        } else {
            mList = new ArrayList<>();
            for (DownloadInfo info : mBackList) {
                if (info.category == mSelectedCategory) {
                    mList.add(info);
                }
            }
        }
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
        updateTitle();
        mPaginationController.updatePaginationIndicator();
        updateView();
        mPaginationController.queryUnreadSpiderInfo();
    }
    private void refreshNasCatalog() {
        Context context = getEHContext();
        if (context == null) return;
        mNasCatalogMap.clear();
        if (!NasConfigStore.isEnabled(context)) return;
        for (NasCatalogEntry entry : NasCatalogStore.load(context)) {
            if (NasCatalogStore.isHiddenLocally(context, entry.gid)
                    && (mDownloadManager == null
                    || mDownloadManager.getDownloadInfo(entry.gid) == null)) continue;
            mNasCatalogMap.put(entry.gid, entry);
        }
    }

    public long getEffectiveLabelCount(@Nullable String label) {
        if (mDownloadManager == null) return 0L;
        if (label != null) {
            long count = mDownloadManager.getLabelCount(label);
            refreshNasCatalog();
            for (NasCatalogEntry entry : mNasCatalogMap.values()) {
                if (label.equals(entry.label)
                        && mDownloadManager.getDownloadInfo(entry.gid) == null) count++;
            }
            return count;
        }
        long count = mDownloadManager.getDefaultDownloadInfoList().size();
        Context context = getEHContext();
        if (context == null || !NasConfigStore.isEnabled(context)) return count;
        refreshNasCatalog();
        for (NasCatalogEntry entry : mNasCatalogMap.values()) {
            if (entry.label.isEmpty()
                    && mDownloadManager.getDownloadInfo(entry.gid) == null) count++;
        }
        return count;
    }

    private void setPaginationDrawerLock(boolean active) {
        boolean keepLocked = active
                || (mRecyclerView != null && mRecyclerView.isInCustomChoice())
                || (mFabLayout != null && mFabLayout.isExpanded());
        int mode = keepLocked ? DrawerLayout.LOCK_MODE_LOCKED_CLOSED
                : DrawerLayout.LOCK_MODE_UNLOCKED;
        setDrawerLockMode(mode, Gravity.LEFT);
        setDrawerLockMode(mode, Gravity.RIGHT);
    }

    public void onMenuCreated(@NonNull android.view.Menu menu) {
        Context context = getEHContext();
        boolean nasEnabled = context != null && NasConfigStore.isEnabled(context);
        MenuItem upload = menu.findItem(R.id.action_nas_upload_all);
        MenuItem metadata = menu.findItem(R.id.action_nas_upload_metadata);
        MenuItem download = menu.findItem(R.id.action_nas_download_all);
        MenuItem downloadMetadata = menu.findItem(R.id.action_nas_download_metadata);
        if (upload != null) upload.setVisible(nasEnabled);
        if (metadata != null) metadata.setVisible(nasEnabled);
        if (download != null) download.setVisible(nasEnabled);
        if (downloadMetadata != null) downloadMetadata.setVisible(nasEnabled);
    }

    private void confirmBulkNasSync(@NonNull NasSyncEngine.Direction direction) {
        Context context = getEHContext();
        if (context == null) return;
        if (!NasConfigStore.isEnabled(context)) {
            Toast.makeText(context, R.string.settings_nas_disabled, Toast.LENGTH_SHORT).show();
            return;
        }
        final int title;
        final int message;
        final String action;
        if (direction == NasSyncEngine.Direction.UPLOAD_TO_NAS) {
            title = R.string.nas_upload_all;
            message = R.string.nas_upload_all_confirm;
            action = NasSyncService.ACTION_UPLOAD_ALL;
        } else if (direction == NasSyncEngine.Direction.UPLOAD_DATABASE_TO_NAS) {
            title = R.string.nas_upload_metadata;
            message = R.string.nas_upload_metadata_confirm;
            action = NasSyncService.ACTION_UPLOAD_METADATA;
        } else {
            title = R.string.nas_download_all;
            message = R.string.nas_download_all_confirm;
            action = NasSyncService.ACTION_DOWNLOAD_ALL;
        }
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    NasSyncService.start(context, action);
                    Toast.makeText(context, R.string.nas_sync_started_background,
                            Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    public boolean readDownload(@NonNull DownloadInfo downloadInfo) {
        Activity activity = getActivity2();
        Context context = getEHContext();
        if (activity == null || context == null) return false;
        if (isEffectivelyNasOnly(downloadInfo)) {
            readNasGallery(downloadInfo);
            return true;
        }

        Intent intent = new Intent(activity, GalleryActivity.class);
        if (downloadInfo.archiveUri != null && downloadInfo.archiveUri.startsWith("content://")) {
            Uri archiveUri = Uri.parse(downloadInfo.archiveUri);
            try {
                try (InputStream testStream = context.getContentResolver().openInputStream(archiveUri)) {
                    if (testStream == null) {
                        Toast.makeText(context, R.string.archive_not_accessible,
                                Toast.LENGTH_SHORT).show();
                        return true;
                    }
                }
            } catch (SecurityException e) {
                try {
                    context.getContentResolver().takePersistableUriPermission(archiveUri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (Exception ex) {
                    Toast.makeText(context, R.string.archive_permission_lost,
                            Toast.LENGTH_LONG).show();
                    Analytics.recordException(ex);
                    return true;
                }
            } catch (Exception e) {
                Toast.makeText(context, R.string.archive_not_accessible,
                        Toast.LENGTH_SHORT).show();
                return true;
            }
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(archiveUri);
        } else {
            intent.setAction(GalleryActivity.ACTION_EH);
            intent.putExtra(GalleryActivity.KEY_GALLERY_INFO, downloadInfo);
        }
        galleryActivityLauncher.launch(intent);
        return true;
    }

    public void requestNasGallerySync(@NonNull DownloadInfo info) {
        Context context = getEHContext();
        NasCatalogEntry entry = mNasCatalogMap.get(info.gid);
        if (context == null || entry == null) return;
        new AlertDialog.Builder(context)
                .setTitle(R.string.nas_gallery_sync_title)
                .setMessage(getString(R.string.nas_gallery_sync_message, entry.title))
                .setPositiveButton(R.string.nas_gallery_sync_action, (dialog, which) -> {
                    NasSyncService.downloadGallery(context, entry.gid);
                    Toast.makeText(context, R.string.nas_sync_started_background,
                            Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    public void openNasGallery(@NonNull DownloadInfo info) {
        NasCatalogEntry entry = mNasCatalogMap.get(info.gid);
        if (entry == null) return;
        if (info.token != null && !info.token.isEmpty()) {
            openDownloadDetails(info, null);
            return;
        }
        new NasGalleryPrepareTask(this, entry, false)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void readNasGallery(@NonNull DownloadInfo info) {
        NasCatalogEntry entry = mNasCatalogMap.get(info.gid);
        if (entry == null) return;
        // The existing EhGalleryProvider can stream missing pages through NasPageFetcher,
        // but it needs the gallery's .ehviewer metadata in the local download directory first.
        new NasGalleryPrepareTask(this, entry, true)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void openDownloadReader(@NonNull DownloadInfo info) {
        Activity activity = getActivity2();
        if (activity == null) return;
        Intent intent = new Intent(activity, GalleryActivity.class);
        intent.setAction(GalleryActivity.ACTION_EH);
        intent.putExtra(GalleryActivity.KEY_GALLERY_INFO, info);
        galleryActivityLauncher.launch(intent);
    }

    private void openDownloadDetails(@NonNull DownloadInfo info, @Nullable View transitionView) {
        Bundle args = new Bundle();
        args.putString(GalleryDetailScene.KEY_ACTION,
                GalleryDetailScene.ACTION_DOWNLOAD_GALLERY_INFO);
        args.putParcelable(GalleryDetailScene.KEY_GALLERY_INFO, info);
        Announcer announcer = new Announcer(GalleryDetailScene.class).setArgs(args);
        if (transitionView != null) {
            announcer.setTranHelper(new EnterGalleryDetailTransaction(transitionView));
        }
        startScene(announcer);
    }

    private static String nasErrorMessage(@NonNull DownloadsScene scene, Throwable error) {
        return scene.getString(R.string.nas_gallery_sync_failed_prefix) + errorDetail(error);
    }

    private static String errorDetail(Throwable error) {
        StringBuilder detail = new StringBuilder();
        Throwable current = error;
        for (int depth = 0; current != null && depth < 4; depth++, current = current.getCause()) {
            if (detail.length() > 0) detail.append(" → ");
            detail.append(current.getClass().getSimpleName());
            String message = current.getLocalizedMessage();
            if (!TextUtils.isEmpty(message)) detail.append(": ").append(message);
        }
        if (detail.length() == 0) detail.append("Unknown error");
        return detail.toString();
    }

    public NasCatalogEntry getNasCatalogEntry(long gid) {
        return mNasCatalogMap.get(gid);
    }

    public boolean isGalleryAvailableLocally(@NonNull DownloadInfo info) {
        if (info.archiveUri != null && info.archiveUri.startsWith("content://")) return true;
        NasCatalogEntry entry = mNasCatalogMap.get(info.gid);
        // Entries outside the NAS catalog retain the original local-download behavior.
        if (entry == null) return true;
        if (info.state == DownloadInfo.STATE_WAIT || info.state == DownloadInfo.STATE_DOWNLOAD) {
            return true;
        }
        Boolean cached = mLocalAvailability.get(info.gid);
        if (cached != null) return cached;
        UniFile root = Settings.getDownloadLocation();
        UniFile directory = root != null ? root.findFile(entry.directoryName) : null;
        // A database imported from another device may leave an empty directory or only a
        // thumbnail behind. Neither means the comic itself is stored on this device.
        boolean available = directory != null && directory.isDirectory()
                && findImageFile(directory, 0) != null;
        mLocalAvailability.put(info.gid, available);
        return available;
    }

    private boolean isEffectivelyNasOnly(@NonNull DownloadInfo info) {
        return mNasCatalogMap.containsKey(info.gid) && !isGalleryAvailableLocally(info);
    }

    private static void deleteFileAsync(UniFile... files) {
        new AsyncTask<UniFile, Void, Void>() {
            @Override
            protected Void doInBackground(UniFile... params) {
                for (UniFile file : params) {
                    if (file != null) {
                        file.delete();
                    }
                }
                return null;
            }
        }.executeOnExecutor(IoThreadPoolExecutor.Companion.getInstance(), files);
    }

    private static void deleteGalleryFilesAsync(List<? extends GalleryInfo> galleryInfoList) {
        new AsyncTask<List<? extends GalleryInfo>, Void, Void>() {
            @Override
            protected Void doInBackground(List<? extends GalleryInfo>... params) {
                for (GalleryInfo info : params[0]) {
                    UniFile file = getGalleryDownloadDir(info);
                    EhDB.removeDownloadDirname(info.gid);
                    if (file != null) {
                        file.delete();
                    }
                }
                return null;
            }
        }.executeOnExecutor(IoThreadPoolExecutor.Companion.getInstance(), galleryInfoList);
    }

    private void setDragEnable(FloatingActionButton fab) {
        DRAG_ENABLE = !DRAG_ENABLE;
        Settings.setDragDownloadGallery(DRAG_ENABLE);
        Context context = getEHContext();
        if (null == context) return;
        if (DRAG_ENABLE) {
            fab.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.v_mobile_hand_left_x24, context.getTheme()));
        } else {
            fab.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.v_mobile_hand_left_off_x24, context.getTheme()));
        }
//        mDragDropManager.cancelDrag(dragEnable);
    }

    private void viewRandom() {
        List<DownloadInfo> list = mList;
        if (list == null) {
            return;
        }
        int position = (int) (Math.random() * list.size());
        if (position < 0 || position >= list.size()) {
            return;
        }
        Activity activity = getActivity2();
        if (null == activity || null == mRecyclerView) {
            return;
        }

        DownloadInfo info = list.get(position);
        if (isEffectivelyNasOnly(info)) {
            readNasGallery(info);
        } else {
            Intent intent = new Intent(activity, GalleryActivity.class);
            intent.setAction(GalleryActivity.ACTION_EH);
            intent.putExtra(GalleryActivity.KEY_GALLERY_INFO, info);
            galleryActivityLauncher.launch(intent);
        }
    }
    public void onClickNasSecondaryFab(FabLayout view, FloatingActionButton fab, int position) {
        Context context = getEHContext();
        Activity activity = getActivity2();
        MyEasyRecyclerView recyclerView = mRecyclerView;
        if (null == context || null == activity || null == recyclerView) {
            return;
        }

        if (0 == position) {
            recyclerView.checkAll();
        } else {
            List<DownloadInfo> list = mList;
            if (list == null) {
                return;
            }

            LongList gidList = null;
            List<DownloadInfo> downloadInfoList = null;
            boolean collectGid = position == 1 || position == 2 || position == 3; // Start, Stop, Delete
            boolean collectDownloadInfo = position >= 1 && position <= 4;
            // Start needs the objects too, so NAS-only rows can use the SMB download path.
            if (collectGid) {
                gidList = new LongList();
            }
            if (collectDownloadInfo) {
                downloadInfoList = new LinkedList<>();
            }

            SparseBooleanArray stateArray = recyclerView.getCheckedItemPositions();
            for (int i = 0, n = stateArray.size(); i < n; i++) {
                if (stateArray.valueAt(i)) {
                    DownloadInfo info = list.get(positionInList(stateArray.keyAt(i)));
                    if (collectDownloadInfo) {
                        downloadInfoList.add(info);
                    }
                    if (collectGid) {
                        gidList.add(info.gid);
                    }
                }
            }

            switch (position) {
                case 1: { // Start
                    if (downloadInfoList.isEmpty()) {
                        break;
                    }
                    LongList localGids = new LongList();
                    for (DownloadInfo info : downloadInfoList) {
                        if (isEffectivelyNasOnly(info)) {
                            NasSyncService.downloadGallery(context, info.gid);
                        } else {
                            localGids.add(info.gid);
                        }
                    }
                    if (!localGids.isEmpty()) {
                        Intent intent = new Intent(activity, DownloadService.class);
                        intent.setAction(DownloadService.ACTION_START_RANGE);
                        intent.putExtra(DownloadService.KEY_GID_LIST, localGids);
                        activity.startService(intent);
                    }
                    // Cancel check mode
                    recyclerView.outOfCustomChoiceMode();
                    break;
                }
                case 2: { // Stop
                    if (downloadInfoList.isEmpty()) {
                        break;
                    }
                    LongList localGids = new LongList();
                    for (DownloadInfo info : downloadInfoList) {
                        if (isEffectivelyNasOnly(info)) {
                            NasSyncService.cancelGallery(context, info.gid);
                        } else {
                            localGids.add(info.gid);
                        }
                    }
                    if (null != mDownloadManager && !localGids.isEmpty()) {
                        mDownloadManager.stopRangeDownload(localGids);
                    }
                    // Cancel check mode
                    recyclerView.outOfCustomChoiceMode();
                    break;
                }
                case 3: { // Delete
                    if (downloadInfoList.isEmpty()) {
                        break;
                    }
                    boolean containsNas = false;
                    for (DownloadInfo info : downloadInfoList) {
                        if (mNasCatalogMap.containsKey(info.gid)) {
                            containsNas = true;
                            break;
                        }
                    }
                    if (containsNas) {
                        DeleteRangeDialogHelper helper = new DeleteRangeDialogHelper(
                                downloadInfoList, null);
                        new AlertDialog.Builder(context)
                                .setTitle(R.string.download_remove_dialog_title)
                                .setMessage(R.string.nas_delete_scope_message)
                                .setNegativeButton(android.R.string.cancel, null)
                                .setNeutralButton(R.string.nas_delete_local_only,
                                        (dialog, which) -> helper.performDelete(false, true))
                                .setPositiveButton(R.string.nas_delete_local_and_remote,
                                        (dialog, which) -> helper.performDelete(true, true))
                                .show();
                        break;
                    }
                    CheckBoxDialogBuilder builder = new CheckBoxDialogBuilder(context,
                            getString(R.string.download_remove_dialog_message_2, gidList.size()),
                            getString(R.string.download_remove_dialog_check_text),
                            Settings.getRemoveImageFiles());
                    DeleteRangeDialogHelper helper = new DeleteRangeDialogHelper(
                            downloadInfoList, builder);
                    builder.setTitle(R.string.download_remove_dialog_title)
                            .setPositiveButton(android.R.string.ok, helper)
                            .show();
                    break;
                }
                case 4: {// Move
                    if (downloadInfoList.isEmpty()) {
                        break;
                    }
                    List<DownloadLabel> labelRawList = EhApplication.getDownloadManager(context).getLabelList();
                    List<String> labelList = new ArrayList<>(labelRawList.size() + 1);
                    labelList.add(getString(R.string.default_download_label_name));
                    for (int i = 0, n = labelRawList.size(); i < n; i++) {
                        labelList.add(labelRawList.get(i).getLabel());
                    }
                    String[] labels = labelList.toArray(new String[labelList.size()]);

                    MoveDialogHelper helper = new MoveDialogHelper(labels, downloadInfoList);

                    new AlertDialog.Builder(context)
                            .setTitle(R.string.download_move_dialog_title)
                            .setItems(labels, helper)
                            .show();
                    break;
                }
                case 5:
                    if (mList == null || mList.isEmpty()) {
                        return;
                    }
                    onClickPrimaryFab(mFabLayout, null);
                    viewRandom();
                    break;
                case 6:
                    setDragEnable(fab);
                    break;
            }
        }
    }
    private class DeleteRangeDialogHelper implements DialogInterface.OnClickListener {

        private final List<DownloadInfo> mDownloadInfoList;
        @Nullable private final CheckBoxDialogBuilder mBuilder;

        public DeleteRangeDialogHelper(List<DownloadInfo> downloadInfoList,
                                       @Nullable CheckBoxDialogBuilder builder) {
            mDownloadInfoList = downloadInfoList;
            mBuilder = builder;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (which != DialogInterface.BUTTON_POSITIVE) {
                return;
            }
            performDelete(false, mBuilder != null && mBuilder.isChecked());
        }

        void performDelete(boolean deleteRemote, boolean deleteLocalFiles) {

            // Cancel check mode
            if (mRecyclerView != null) {
                mRecyclerView.outOfCustomChoiceMode();
            }

            Context context = getEHContext();
            LongList managedGids = new LongList();
            List<DownloadInfo> localFiles = new ArrayList<>();
            int hiddenNasEntries = 0;
            for (DownloadInfo info : mDownloadInfoList) {
                boolean nasOnly = isEffectivelyNasOnly(info);
                boolean storedOnNas = mNasCatalogMap.containsKey(info.gid);
                if (storedOnNas && deleteRemote && context != null) {
                    NasSyncService.cancelGallery(context, info.gid);
                    NasCatalogStore.hideLocally(context, info.gid);
                    NasSyncService.deleteGallery(context, info.gid);
                    hiddenNasEntries++;
                } else if (nasOnly) {
                    if (context != null) {
                        NasCatalogStore.hideLocally(context, info.gid);
                        NasSyncService.cancelGallery(context, info.gid);
                    }
                    // Dragging or moving can materialize a synthetic NAS row in the database
                    // before DownloadManager owns it. Remove that row as part of the local hide.
                    EhDB.removeDownloadInfo(info.gid);
                    EhDB.removeDownloadDirname(info.gid);
                    hiddenNasEntries++;
                }
                if (mDownloadManager != null
                        && mDownloadManager.getDownloadInfo(info.gid) != null) {
                    managedGids.add(info.gid);
                }
                if (!nasOnly) {
                    localFiles.add(info);
                }
            }

            // Local-only hides synthetic NAS rows; the explicit remote option queues a guarded
            // SMB deletion and updates the canonical metadata database in the background.
            if (null != mDownloadManager && !managedGids.isEmpty()) {
                mDownloadManager.deleteRangeDownload(managedGids);
            }

            // Delete image files
            Settings.putRemoveImageFiles(deleteLocalFiles);
            if (deleteLocalFiles) {
                if (context != null) {
                    for (DownloadInfo info : localFiles) {
                        if (NasCatalogStore.find(context, info.gid) != null) {
                            NasCatalogStore.markRemoteOnly(context, info.gid);
                        }
                    }
                }
                deleteGalleryFilesAsync(localFiles);
            }
            if (hiddenNasEntries > 0 && context != null) {
                Toast.makeText(context, deleteRemote
                                ? R.string.nas_gallery_remote_delete_started
                                : R.string.nas_gallery_hidden_locally,
                        Toast.LENGTH_SHORT).show();
                updateForLabel();
                updateView();
            }
        }
    }

    private class MoveDialogHelper implements DialogInterface.OnClickListener {

        private final String[] mLabels;
        private final List<DownloadInfo> mDownloadInfoList;

        public MoveDialogHelper(String[] labels, List<DownloadInfo> downloadInfoList) {
            mLabels = labels;
            mDownloadInfoList = downloadInfoList;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            // Cancel check mode
            Context context = getEHContext();
            if (null == context) {
                return;
            }
            if (null != mRecyclerView) {
                mRecyclerView.outOfCustomChoiceMode();
            }

            String label;
            if (which == 0) {
                label = null;
            } else {
                label = mLabels[which];
            }
            DownloadManager manager = EhApplication.getDownloadManager(context);
            List<DownloadInfo> nasOnly = new ArrayList<>();
            for (DownloadInfo info : mDownloadInfoList) {
                if (isEffectivelyNasOnly(info) && manager.getDownloadInfo(info.gid) == null) {
                    nasOnly.add(info);
                }
            }
            if (!nasOnly.isEmpty()) manager.addDownload(nasOnly);
            List<DownloadInfo> managed = new ArrayList<>(mDownloadInfoList.size());
            for (DownloadInfo info : mDownloadInfoList) {
                DownloadInfo actual = manager.getDownloadInfo(info.gid);
                managed.add(actual != null ? actual : info);
            }
            manager.changeLabel(managed, label);
            try {
                NasCatalogStore.updateFromDownloads(context, managed);
            } catch (IOException error) {
                Log.e(TAG, "Unable to update NAS catalog labels", error);
            }
        }
    }


    private static final class NasGalleryPrepareTask extends AsyncTask<Void, Void, DownloadInfo> {
        private final WeakReference<DownloadsScene> sceneReference;
        private final Context applicationContext;
        private final NasCatalogEntry entry;
        private final boolean openReader;
        private ProgressDialog progressDialog;
        private Throwable error;

        NasGalleryPrepareTask(@NonNull DownloadsScene scene, @NonNull NasCatalogEntry entry,
                              boolean openReader) {
            sceneReference = new WeakReference<>(scene);
            Context context = scene.getEHContext();
            if (context == null) throw new IllegalStateException("Scene is not attached");
            applicationContext = context.getApplicationContext();
            this.entry = entry;
            this.openReader = openReader;
        }

        @Override
        protected void onPreExecute() {
            DownloadsScene scene = sceneReference.get();
            if (scene == null || !scene.isAdded()) return;
            progressDialog = new ProgressDialog(scene.getEHContext());
            progressDialog.setTitle(R.string.nas_gallery_sync_title);
            progressDialog.setMessage(scene.getString(R.string.nas_gallery_sync_preparing));
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(true);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setOnCancelListener(dialog -> cancel(true));
            progressDialog.show();
        }

        @Override
        protected DownloadInfo doInBackground(Void... ignored) {
            NasSyncConfig config = NasConfigStore.load(applicationContext);
            try {
                if (!NasConfigStore.isEnabled(applicationContext)) {
                    throw new IOException("NAS is disabled");
                }
                UniFile localRoot = Settings.getDownloadLocation();
                if (localRoot == null) throw new IOException("Download directory is not configured");
                UniFile metadata = new NasCatalogClient(config).prepareMetadata(localRoot, entry);
                SpiderInfo spiderInfo = SpiderInfo.read(metadata);
                if (spiderInfo == null || spiderInfo.gid != entry.gid) {
                    if (metadata.delete()) {
                        config = NasConfigStore.load(applicationContext);
                        metadata = new NasCatalogClient(config).prepareMetadata(localRoot, entry);
                        spiderInfo = SpiderInfo.read(metadata);
                    }
                }
                if (spiderInfo == null || spiderInfo.gid != entry.gid) {
                    throw new IOException("Invalid .ehviewer metadata in NAS gallery");
                }
                EhDB.putDownloadDirname(entry.gid, entry.directoryName);
                NasCatalogStore.markPartiallyCached(applicationContext, entry.gid);
                DownloadInfo info = NasCatalogStore.asNasOnlyDownload(entry);
                info.token = spiderInfo.token;
                info.total = spiderInfo.pages;
                return info;
            } catch (Throwable throwable) {
                ExceptionUtils.throwIfFatal(throwable);
                error = throwable;
                config.clearPassword();
                return null;
            }
        }

        @Override
        protected void onPostExecute(DownloadInfo info) {
            dismiss();
            DownloadsScene scene = sceneReference.get();
            if (scene == null || !scene.isAdded() || info == null) {
                if (scene != null && scene.isAdded() && error != null) {
                    Toast.makeText(scene.getEHContext(), nasErrorMessage(scene, error),
                            Toast.LENGTH_LONG).show();
                }
                return;
            }
            if (openReader) {
                scene.openDownloadReader(info);
            } else {
                scene.openDownloadDetails(info, null);
            }
            if (scene.mAdapter != null) scene.mAdapter.notifyDataSetChanged();
        }

        @Override protected void onCancelled() { dismiss(); }
        @Override protected void onCancelled(DownloadInfo info) { dismiss(); }

        private void dismiss() {
            if (progressDialog != null && progressDialog.isShowing()) {
                try {
                    progressDialog.dismiss();
                } catch (IllegalArgumentException ignored) {
                }
            }
            progressDialog = null;
        }
    }

}
