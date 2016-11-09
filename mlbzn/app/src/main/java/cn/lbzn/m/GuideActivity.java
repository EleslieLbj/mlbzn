package cn.lbzn.m;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import java.util.ArrayList;
import cn.lbzn.m.Utils.PrefUtils;
import cn.lbzn.m.Utils.Utils;

public class GuideActivity extends BaseActivity {

    private  static final int[] mImage=new int[]{R.drawable.guide_1,R.drawable.guide_4,R.drawable.guide_3};
    private ViewPager vpGuide;
    // 引导小圆点的父控件
    private LinearLayout llPointGroup;
    // 小红点
    private View redPoint;
    // 圆点间的距离
    private  int mPointWith;
    // 开始体验
    private Button btnStart;
    // 创建集合去添加图片
    private ArrayList<ImageView> mImageViewArrayList;
    private static  final  int ENTER_APP=1001;
    //创建handler
    private Handler mHandler= new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case ENTER_APP:
                    // 进入主界面
                    intoMain();
                    break;
                default:
                    break;
            }
        }
    };
    // 进入主界面
    private void intoMain() {
        // 记录点击按钮时的状态
        PrefUtils.setBoolean(GuideActivity.this,"is_user_guide_showed",true);
        // 跳转页面
        //  startActivity(new Intent(GuideActivity.this,LockSetupActivity.class));
        startActivity(new Intent(GuideActivity.this,MainActivity.class));
        // 关闭当前的页面
        finish();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏状态栏
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // 去掉标题
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_guide);
        // 初始化控件
        init();
    }

    private void init() {
        vpGuide = (ViewPager)findViewById(R.id.vp_guide);
        llPointGroup = (LinearLayout)findViewById(R.id.ll_point_group);
        redPoint=findViewById(R.id.view_red_point);
        btnStart = (Button)findViewById(R.id.btn_start);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 记录点击按钮时的状态
                PrefUtils.setBoolean(GuideActivity.this,"is_user_guide_showed",true);

                // 跳转页面
                startActivity(new Intent(GuideActivity.this,MainActivity.class));
                // 关闭当前的页面
                finish();
            }
        });
        // 初始化界面
        initView();
        // 设置适配器
        vpGuide.setAdapter(new GuidePager());
        // 设置滑动监听事件
        vpGuide.setOnPageChangeListener(new GuidePagerListener());

    }

    private void initView() {
        mImageViewArrayList= new ArrayList<ImageView>();
        // 初始化引导页的3个界面
        for(int i=0;i<mImage.length;i++){
            ImageView image= new ImageView(this);
            // 设置引导页背景
            image.setBackgroundResource(mImage[i]);
            // 添加到集合
            mImageViewArrayList.add(image);
        }
        // 初始化引导页的小圆点
        for(int i=0;i<mImage.length;i++){
            View point = new View(this);
            // 设置引导页默认圆点
            point.setBackgroundResource(R.drawable.shape_point_gray);
            // 适配前
            //  LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(10,10);
            // 适配后
            LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(Utils.dip2px(this,10), Utils.dip2px(this,10));
            if(i>0){
                // 设置圆点间距
                // 适配前
                //params.leftMargin=10;
                // 适配后
                params.leftMargin= Utils.dip2px(this,10);
            }
            // 设置圆点大小
            point.setLayoutParams(params);
            // 将小圆点添加给线性布局.
            llPointGroup.addView(point);
        }
        // 获取视图树, 对layout结束事件进行监听
        llPointGroup.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    // 当layout执行结束后回调此方法
                    @Override
                    public void onGlobalLayout() {
                        // 一进来就进行移除操作
                        llPointGroup.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        mPointWith=llPointGroup.getChildAt(1).getLeft()-llPointGroup.getChildAt(0).getLeft();
                    }
                });

    }
    /**
     * 设置ViewPager的适配器
     */
    class GuidePager extends PagerAdapter {
        @Override
        public int getCount() {
            return mImage.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view==object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(mImageViewArrayList.get(position));
            return mImageViewArrayList.get(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }
    /**
     * ViewPager的滑动监听
     */
    class GuidePagerListener implements ViewPager.OnPageChangeListener{
        // 滑动事件
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            int len=(int) (mPointWith*positionOffset)+position*mPointWith;
            // 获取当前红点的布局参数
            RelativeLayout.LayoutParams params= (RelativeLayout.LayoutParams) redPoint.getLayoutParams();
            // 设置左边距
            params.leftMargin=len;
            // 重新给小圆点设置布局参数
            redPoint.setLayoutParams(params);
        }
        // 某个页面被选中
        @Override
        public void onPageSelected(int position) {
            //   if (position==mImage.length-1){// 最后一个页面
            // 显示开始体验按钮
            //       btnStart.setVisibility(View.INVISIBLE);
            //     }else{
            // 隐藏开始按钮
            //        btnStart.setVisibility(View.INVISIBLE);
            //     }

            if (position==mImage.length-1){
                mHandler.sendEmptyMessageDelayed(ENTER_APP,500);
            }
        }
        // 滑动状态发生变化
        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }
}
