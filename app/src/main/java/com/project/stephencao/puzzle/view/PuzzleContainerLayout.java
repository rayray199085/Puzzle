package com.project.stephencao.puzzle.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.project.stephencao.puzzle.R;
import com.project.stephencao.puzzle.bean.ImagePieceBean;
import com.project.stephencao.puzzle.util.ImageSplitter;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PuzzleContainerLayout extends RelativeLayout implements View.OnClickListener {
    private int mColumnCount = 3;
    private int mPaddingWidth; // container inner padding width
    private int mMarginWidth; // distance between child images
    private ImageView[] mImageViews;
    private int mChildImageWidth;
    private Bitmap mImage;
    private int mBoardLength;
    private List<ImagePieceBean> mImagePieces;
    private boolean once = false;
    private int mFirstClickChildId;
    private int mSecondClickChildId;
    private int[] mCorrectOrder;
    private RelativeLayout mAnimationLayout;
    private boolean mIsShowingAnimation = false;
    private static final int MSG_UPDATE_TIMER = 101;
    private static final int MSG_GAME_OVER = 102;
    private static final int MSG_NEXT_STAGE = 103;
    private PuzzleStageListener mPuzzleStageListener;
    private boolean mEnableTimer = true;
    private int mCurrentStage = 0;
    private int mCurrentRank = 0;
    private int[] mAllStageImages = new int[]{R.drawable.image1, R.drawable.image2, R.drawable.image3};
    private boolean mDoWin = false;
    private boolean mDoFail = false;
    private int mTimeLimit;
    private boolean mIsPause = false;

    public void doEnableTimer(boolean enableTimer) {
        mEnableTimer = enableTimer;
    }

    public void setPuzzleStageListener(PuzzleStageListener puzzleStageListener) {
        mPuzzleStageListener = puzzleStageListener;
    }

    public interface PuzzleStageListener {
        void updateTimer(int timeLimit);

        void setProgressBarMaxValue(int maxValue);

        void gameOver(boolean isGameOver);

        void setCurrentStageNotification(int currentStage);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_TIMER: {
                    if (!mDoWin && !mDoFail && !mIsPause) {
                        if (mPuzzleStageListener != null && mTimeLimit > -1) {
                            mPuzzleStageListener.updateTimer(mTimeLimit--);
                            if (mTimeLimit == -1) {
                                mDoFail = true;
                            } else {
                                mHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIMER, 1000);
                            }

                        }
                    }
                    if (mDoFail) {
                        mPuzzleStageListener.gameOver(true);
                    }
                    break;
                }
                case MSG_NEXT_STAGE: {
                    showDialogToNextStage();
                    break;
                }
            }
        }
    };

    public void pauseTheGame(){
        mIsPause = true;
    }

    public void continueTheGame(){
        if(mIsPause){
            mIsPause = false;
            mHandler.sendEmptyMessage(MSG_UPDATE_TIMER);
        }
    }


    private void showDialogToNextStage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("Do you want to play next stage?");
        builder.setCancelable(false);
        builder.setPositiveButton("Why not?", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                toNextStage(false);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Replay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                toNextStage(true);
            }
        });
        builder.show();
    }


    public PuzzleContainerLayout(Context context) {
        this(context, null);
    }

    public PuzzleContainerLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PuzzleContainerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mMarginWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, getResources().getDisplayMetrics());
        mPaddingWidth = Math.min(Math.min(getPaddingBottom(), getPaddingTop()), Math.min(getPaddingLeft(), getPaddingRight()));
        if (mPaddingWidth <= 0) {
            mPaddingWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics());
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mBoardLength = Math.min(getMeasuredHeight(), getMeasuredWidth());
        if (!once) {
            initChildImages();
            setupChildImagesAttributes();
            checkWhetherEnableTimer();
            once = true;
        }
        setMeasuredDimension(mBoardLength, mBoardLength);
    }

    private void checkWhetherEnableTimer() {
        if (mEnableTimer) {
            mTimeLimit = (int) (Math.pow(2, mColumnCount) * 5);
            mHandler.sendEmptyMessage(MSG_UPDATE_TIMER);
            if (mPuzzleStageListener != null) {
                mPuzzleStageListener.setProgressBarMaxValue(mTimeLimit);
            }
        }
    }

    private void initChildImages() {
        setImage();
        mImagePieces = ImageSplitter.splitImage(mImage, mColumnCount);
//        Collections.shuffle(mImagePieces);
        Collections.sort(mImagePieces, new Comparator<ImagePieceBean>() {
            @Override
            public int compare(ImagePieceBean o1, ImagePieceBean o2) {
                return Math.random() > 0.5 ? 1 : -1;
            }
        });
        if (mPuzzleStageListener != null) {
            mPuzzleStageListener.setCurrentStageNotification(mCurrentRank + 1);
        }
    }

    private void setupChildImagesAttributes() {
        mCorrectOrder = new int[mColumnCount * mColumnCount];
        mChildImageWidth = (mBoardLength - mPaddingWidth * 2 - mMarginWidth * (mColumnCount - 1)) / mColumnCount;
        mImageViews = new ImageView[mColumnCount * mColumnCount];
        for (int i = 0; i < mImageViews.length; i++) {
            mImageViews[i] = new ImageView(getContext());
            mImageViews[i].setImageBitmap(mImagePieces.get(i).getBitmap());
            mImageViews[i].setOnClickListener(this);
            mImageViews[i].setId(i + 1);
            mCorrectOrder[i] = i;
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(mChildImageWidth, mChildImageWidth);
            /**
             * set margin between child images
             * set child images' positions
             */
            if (i % mColumnCount == 0) {
                params.leftMargin = mPaddingWidth;
            } else {
                params.leftMargin = mMarginWidth;
                params.addRule(RelativeLayout.RIGHT_OF, mImageViews[i - 1].getId());
            }
            if (i < mColumnCount) {
                params.topMargin = mPaddingWidth;
            } else {
                params.topMargin = mMarginWidth;
                params.addRule(RelativeLayout.BELOW, mImageViews[i - mColumnCount].getId());
            }
            mImageViews[i].setLayoutParams(params);
            addView(mImageViews[i]);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    public void setImage() {
        if (mCurrentStage < mAllStageImages.length) {
            mImage = BitmapFactory.decodeResource(getResources(), mAllStageImages[mCurrentStage]);
        }
    }

    @Override
    public void onClick(View v) {
        if (mIsShowingAnimation) {
            return;
        }
        if (mFirstClickChildId == 0) {
            mFirstClickChildId = v.getId();
            mImageViews[mFirstClickChildId - 1].setColorFilter(Color.parseColor("#55FF0000"));
        } else {
            mSecondClickChildId = v.getId();
            mImageViews[mSecondClickChildId - 1].setColorFilter(Color.parseColor("#55FF0000"));
            exchangeImages();
        }
    }

    private void exchangeImages() {
        ImagePieceBean firstPiece = mImagePieces.get(mFirstClickChildId - 1);
        ImagePieceBean secondPiece = mImagePieces.get(mSecondClickChildId - 1);
        setupAnimationLayout();
        prepareAnimationLayout(firstPiece, secondPiece);
    }

    private void prepareAnimationLayout(final ImagePieceBean firstPiece, final ImagePieceBean secondPiece) {
        final ImageView firstView = new ImageView(getContext());
        final ImageView secondView = new ImageView(getContext());
        firstView.setImageBitmap(firstPiece.getBitmap());
        secondView.setImageBitmap(secondPiece.getBitmap());

        RelativeLayout.LayoutParams firstParams = new RelativeLayout.LayoutParams(mChildImageWidth, mChildImageWidth);
        firstParams.leftMargin = mImageViews[mFirstClickChildId - 1].getLeft();
        firstParams.topMargin = mImageViews[mFirstClickChildId - 1].getTop();
        firstView.setLayoutParams(firstParams);
        mAnimationLayout.addView(firstView);

        RelativeLayout.LayoutParams secondParams = new RelativeLayout.LayoutParams(mChildImageWidth, mChildImageWidth);
        secondParams.leftMargin = mImageViews[mSecondClickChildId - 1].getLeft();
        secondParams.topMargin = mImageViews[mSecondClickChildId - 1].getTop();
        secondView.setLayoutParams(secondParams);
        mAnimationLayout.addView(secondView);

        TranslateAnimation firstAnim = new TranslateAnimation(0,
                secondParams.leftMargin - firstParams.leftMargin, 0,
                secondParams.topMargin - firstParams.topMargin);

        firstAnim.setDuration(300);
        firstAnim.setFillAfter(true);
        firstView.startAnimation(firstAnim);

        TranslateAnimation secondAnim = new TranslateAnimation(0,
                firstParams.leftMargin - secondParams.leftMargin, 0,
                firstParams.topMargin - secondParams.topMargin);
        secondAnim.setDuration(300);
        secondAnim.setFillAfter(true);
        secondView.startAnimation(secondAnim);

        firstAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mIsShowingAnimation = true;
                mImageViews[mFirstClickChildId - 1].setVisibility(INVISIBLE);
                mImageViews[mSecondClickChildId - 1].setVisibility(INVISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mImagePieces.set(mFirstClickChildId - 1, secondPiece);
                mImagePieces.set(mSecondClickChildId - 1, firstPiece);
                mImageViews[mFirstClickChildId - 1].setImageBitmap(mImagePieces.get(mFirstClickChildId - 1).getBitmap());
                mImageViews[mSecondClickChildId - 1].setImageBitmap(mImagePieces.get(mSecondClickChildId - 1).getBitmap());
                mImageViews[mFirstClickChildId - 1].setVisibility(VISIBLE);
                mImageViews[mSecondClickChildId - 1].setVisibility(VISIBLE);
                mImageViews[mFirstClickChildId - 1].setColorFilter(null);
                mImageViews[mSecondClickChildId - 1].setColorFilter(null);
                mFirstClickChildId = 0;
                mSecondClickChildId = 0;
                mAnimationLayout.removeAllViews();
                mIsShowingAnimation = false;
                checkWhetherWin();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

    }

    private void setupAnimationLayout() {
        if (mAnimationLayout == null) {
            mAnimationLayout = new RelativeLayout(getContext());
            addView(mAnimationLayout);
        }
    }

    private void checkWhetherWin() {
        boolean doesWin = true;
        for (int i = 0; i < mImagePieces.size(); i++) {
            if (mCorrectOrder[i] != mImagePieces.get(i).getIndex()) {
                doesWin = false;
            }
        }
        if (doesWin) {
            mDoWin = true;
            mHandler.sendEmptyMessage(MSG_NEXT_STAGE);
        }
    }

    public void toNextStage(boolean doRestart) {
        this.removeAllViews();
        mAnimationLayout = null;
        if (!doRestart) {
            mCurrentStage++;
            mCurrentRank++;
        }
        if (mCurrentStage >= mAllStageImages.length) {
            mCurrentStage = 0;
            mColumnCount++;
        }
        mDoWin = false;
        mDoFail = false;
        initChildImages();
        setupChildImagesAttributes();
        checkWhetherEnableTimer();
    }
}
