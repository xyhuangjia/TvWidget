package org.evilbinary.tv.widget;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.AdapterView;

import androidx.recyclerview.widget.RecyclerView;

import org.evilbinary.tv.widget.BorderView.Effect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 作者:evilbinary on 3/26/16.
 * 邮箱:rootdebug@163.com
 */
public class BorderEffect implements Effect {
    private String TAG = BorderEffect.class.getSimpleName();


    protected boolean mScalable = true;
    protected float mScale = 1.1f;

    protected long mDurationTranslate = 200;
    protected int mMargin = 0;
    protected View lastFocus, oldLastFocus;
    protected AnimatorSet mAnimatorSet;
    protected List<Animator> mAnimatorList = new ArrayList<Animator>();
    protected View mTarget;

    protected boolean mEnableTouch = true;

    public BorderEffect() {
        mFocusListener.add(focusMoveListener);
        mFocusListener.add(focusScaleListener);
        mFocusListener.add(focusPlayListener);
        mFocusListener.add(absListViewFocusListener);
    }

    public interface FocusListener {
        public void onFocusChanged(View oldFocus, View newFocus);
    }

    protected List<FocusListener> mFocusListener = new ArrayList<FocusListener>(1);
    protected List<Animator.AnimatorListener> mAnimatorListener = new ArrayList<Animator.AnimatorListener>(1);


    public FocusListener focusScaleListener = new FocusListener() {
        @Override
        public void onFocusChanged(View oldFocus, View newFocus) {
            mAnimatorList.addAll(getScaleAnimator(newFocus, true));
            if (oldFocus != null) {
                mAnimatorList.addAll(getScaleAnimator(oldFocus, false));
            }
        }
    };
    public FocusListener focusPlayListener = new FocusListener() {
        @Override
        public void onFocusChanged(View oldFocus, View newFocus) {
            try {
                if (newFocus instanceof AbsListView) {
                    return;
                }
                AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.setInterpolator(new DecelerateInterpolator(1));
                animatorSet.setDuration(mDurationTranslate);
                animatorSet.playTogether(mAnimatorList);
                for (Animator.AnimatorListener listener : mAnimatorListener) {
                    animatorSet.addListener(listener);
                }
                mAnimatorSet = animatorSet;
                if (oldFocus == null) {
                    animatorSet.setDuration(0);
                    mTarget.setVisibility(View.VISIBLE);
                }
                animatorSet.start();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    };
    public FocusListener focusMoveListener = new FocusListener() {
        @Override
        public void onFocusChanged(View oldFocus, View newFocus) {
            if (newFocus == null) return;
            try {

                mAnimatorList.addAll(getMoveAnimator(newFocus, 0, 0));

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    };


    public FocusListener absListViewFocusListener = new FocusListener() {

        @Override
        public void onFocusChanged(View oldFocus, View newFocus) {

            try {
                Log.d(TAG, "onFocusChanged==>" + oldFocus + " " + newFocus);
                if (oldFocus == null) {
                    for (int i = 0; i < attacheViews.size(); i++) {
                        View view = attacheViews.get(i);
                        if (view instanceof AbsListView) {
                            final AbsListView absListView = (AbsListView) view;
                            mTarget.setVisibility(View.INVISIBLE);
                            if (mFirstFocus) {
                                absListView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                                    @Override
                                    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                                        try {
                                            absListView.removeOnLayoutChangeListener(this);
                                            int factorX = 0, factorY = 0;
                                            Rect rect = new Rect();
                                            View firstView = (View) absListView.getSelectedView();
                                            firstView.getLocalVisibleRect(rect);
                                            if (Math.abs(rect.left - rect.right) > firstView.getMeasuredWidth()) {
                                                factorX = (Math.abs(rect.left - rect.right) - firstView.getMeasuredWidth()) / 2 - 1;
                                                factorY = (Math.abs(rect.top - rect.bottom) - firstView.getMeasuredHeight()) / 2;
                                            }
                                            List<Animator> animatorList = new ArrayList<Animator>(3);
                                            animatorList.addAll(getScaleAnimator(firstView, true));
                                            animatorList.addAll(getMoveAnimator(firstView, factorX, factorY));
                                            mTarget.setVisibility(View.VISIBLE);
                                            AnimatorSet animatorSet = new AnimatorSet();
                                            animatorSet.setDuration(0);
                                            animatorSet.playTogether(animatorList);
                                            animatorSet.start();
                                        } catch (Exception ex) {
                                            ex.printStackTrace();
                                        }
                                    }
                                });


                            }
                            break;
                        }
                    }
                } else if (oldFocus instanceof AbsListView && newFocus instanceof AbsListView) {
                    if (attacheViews.indexOf(oldFocus) >= 0 && attacheViews.indexOf(newFocus) >= 0) {

                        AbsListView a = (AbsListView) oldFocus;
                        AbsListView b = (AbsListView) newFocus;

                        MyOnItemSelectedListener oldOn = (MyOnItemSelectedListener) onItemSelectedListenerList.get(oldFocus);
                        MyOnItemSelectedListener newOn = (MyOnItemSelectedListener) onItemSelectedListenerList.get(newFocus);


                        int factorX = 0, factorY = 0;
                        Rect rect = new Rect();
                        View firstView = (View) b.getSelectedView();
                        firstView.getLocalVisibleRect(rect);
                        if (Math.abs(rect.left - rect.right) > firstView.getMeasuredWidth()) {
                            factorX = (Math.abs(rect.left - rect.right) - firstView.getMeasuredWidth()) / 2 - 1;
                            factorY = (Math.abs(rect.top - rect.bottom) - firstView.getMeasuredHeight()) / 2;
                        }

                        List<Animator> animatorList = new ArrayList<Animator>(3);
                        animatorList.addAll(getScaleAnimator(firstView, true));
                        animatorList.addAll(getScaleAnimator(a.getSelectedView(), false));

                        animatorList.addAll(getMoveAnimator(firstView, factorX, factorY));
                        mTarget.setVisibility(View.VISIBLE);

                        mAnimatorSet = new AnimatorSet();


                        mAnimatorSet.setDuration(mDurationTranslate);
                        mAnimatorSet.playTogether(animatorList);
                        mAnimatorSet.start();

                        oldOn.oldFocus = null;
                        oldOn.newFocus = null;

                        newOn.oldFocus = null;
                        if (newOn.newFocus != null && newOn.oldFocus != null) {
                            newOn.newFocus = null;
                        } else {
                            newOn.newFocus = b.getSelectedView();
                        }

                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    };

    protected List<Animator> getScaleAnimator(View view, boolean isScale) {

        List<Animator> animatorList = new ArrayList<Animator>(2);
        if (!mScalable) return animatorList;
        try {
            float scaleBefore = 1.0f;
            float scaleAfter = mScale;
            if (!isScale) {
                scaleBefore = mScale;
                scaleAfter = 1.0f;
            }
            ObjectAnimator scaleX = new ObjectAnimator().ofFloat(view, "scaleX", scaleBefore, scaleAfter);
            ObjectAnimator scaleY = new ObjectAnimator().ofFloat(view, "scaleY", scaleBefore, scaleAfter);
            animatorList.add(scaleX);
            animatorList.add(scaleY);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return animatorList;
    }

    protected List<Animator> getMoveAnimator(View newFocus, int factorX, int factorY) {

        List<Animator> animatorList = new ArrayList<Animator>();
        int newXY[];
        int oldXY[];

        try {

            newXY = getLocation(newFocus);
            oldXY = getLocation(mTarget);

            int newWidth;
            int newHeight;
            int oldWidth = mTarget.getMeasuredWidth();
            int oldHeight = mTarget.getMeasuredHeight();

            if (mScalable) {
                float scaleWidth = newFocus.getMeasuredWidth() * mScale;
                float scaleHeight = newFocus.getMeasuredHeight() * mScale;
                newWidth = (int) (scaleWidth + mMargin * 2 + 0.5);
                newHeight = (int) (scaleHeight + mMargin * 2 + 0.5);
                newXY[0] = (int) (newXY[0] - (newWidth - newFocus.getMeasuredWidth()) / 2.0f) + factorX;
                newXY[1] = (int) (newXY[1] - (newHeight - newFocus.getMeasuredHeight()) / 2.0f + 0.5 + factorY);

            } else {
                newWidth = newFocus.getWidth();
                newHeight = newFocus.getHeight();
            }
            if (oldHeight == 0 && oldWidth == 0) {
                oldHeight = newHeight;
                oldWidth = newWidth;
            }

            PropertyValuesHolder valuesWidthHolder = PropertyValuesHolder.ofInt("width", oldWidth, newWidth);
            PropertyValuesHolder valuesHeightHolder = PropertyValuesHolder.ofInt("height", oldHeight, newHeight);
            PropertyValuesHolder valuesXHolder = PropertyValuesHolder.ofFloat("translationX", oldXY[0], newXY[0]);
            PropertyValuesHolder valuesYHolder = PropertyValuesHolder.ofFloat("translationY", oldXY[1], newXY[1]);
            final ObjectAnimator scaleAnimator = ObjectAnimator.ofPropertyValuesHolder(mTarget, valuesWidthHolder, valuesHeightHolder, valuesYHolder, valuesXHolder);

            scaleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public synchronized void onAnimationUpdate(ValueAnimator animation) {
                    int width = (int) animation.getAnimatedValue("width");
                    int height = (int) animation.getAnimatedValue("height");
                    float translationX = (float) animation.getAnimatedValue("translationX");
                    float translationY = (float) animation.getAnimatedValue("translationY");
                    //Log.d(TAG,"width:"+width+" height:"+height+" translationX:"+translationX+" translationY:"+translationY);
                    View view = (View) scaleAnimator.getTarget();
                    assert view != null;
                    int w = view.getLayoutParams().width;
                    view.getLayoutParams().width = width;
                    view.getLayoutParams().height = height;
                    if (width > 0) {
                        view.requestLayout();
                        view.postInvalidate();

                    }
                }
            });
            animatorList.add(scaleAnimator);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return animatorList;
    }

    protected int[] getLocation(View view) {
        int[] location = new int[2];
        try {
            view.getLocationOnScreen(location);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return location;
    }

    public void addOnFocusChanged(FocusListener focusListener) {
        this.mFocusListener.add(focusListener);
    }

    public void removeOnFocusChanged(FocusListener focusListener) {
        this.mFocusListener.remove(focusListener);
    }

    public void addAnimatorListener(Animator.AnimatorListener animatorListener) {
        this.mAnimatorListener.add(animatorListener);
    }

    public void removeAnimatorListener(Animator.AnimatorListener animatorListener) {
        this.mAnimatorListener.remove(animatorListener);
    }

    private class VisibleScope {
        public boolean isVisible;
        public View oldFocus;
        public View newFocus;
    }

    protected VisibleScope checkVisibleScope(View oldFocus, View newFocus) {
        VisibleScope scope = new VisibleScope();
        try {
            scope.oldFocus = oldFocus;
            scope.newFocus = newFocus;
            scope.isVisible = true;
            if (attacheViews.indexOf(oldFocus) >= 0 && attacheViews.indexOf(newFocus) >= 0) {
                return scope;
            }

            if (oldFocus != null && newFocus != null) {
                if (oldFocus.getParent() != newFocus.getParent()) {
                    Log.d(TAG, "=====>" + attacheViews.indexOf(newFocus.getParent()) + "=" + attacheViews.indexOf(oldFocus.getParent()));

                    if ((!attacheViews.contains(newFocus.getParent())) || (!attacheViews.contains(oldFocus.getParent()) && attacheViews.indexOf(newFocus.getParent()) > 0)) {
                        mTarget.setVisibility(View.INVISIBLE);
                        AnimatorSet animatorSet = new AnimatorSet();
                        animatorSet.playTogether(getScaleAnimator(oldFocus, false));
                        animatorSet.setDuration(0).start();
                        Log.d(TAG, "=====>1");
                        scope.isVisible = false;
                        return scope;
                    } else {
                        Log.d(TAG, "=====>2");

                        mTarget.setVisibility(View.VISIBLE);
                    }
                    if (!attacheViews.contains(oldFocus.getParent())) {
                        scope.oldFocus = null;
                    }

                } else {
                    if (attacheViews.indexOf(newFocus.getParent()) < 0) {
                        mTarget.setVisibility(View.INVISIBLE);
                        Log.d(TAG, "=====>3");
                        scope.isVisible = false;
                        return scope;
                    }
                    Log.d(TAG, "=====>4");

                }
                Log.d(TAG, "=====>5");
            }
            mTarget.setVisibility(View.VISIBLE);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return scope;
    }

    @Override
    public void onFocusChanged(View target, View oldFocus, View newFocus) {
        try {
            Log.d(TAG, "onFocusChanged:" + oldFocus + "=" + newFocus);

            if (newFocus == null && attacheViews.indexOf(newFocus) >= 0) {
                return;
            }
            if (oldFocus == newFocus) return;

            if (mAnimatorSet != null && mAnimatorSet.isRunning()) {
                mAnimatorSet.end();
            }

            lastFocus = newFocus;
            oldLastFocus = oldFocus;
            mTarget = target;
            Log.d(TAG, "onFocusChanged:111111111" + oldFocus + "=" + newFocus);

            VisibleScope scope = checkVisibleScope(oldFocus, newFocus);
            if (!scope.isVisible) {
                return;
            } else {
                oldFocus = scope.oldFocus;
                newFocus = scope.newFocus;
                oldLastFocus = scope.oldFocus;
            }

            if (isScrolling || newFocus == null || newFocus.getWidth() <= 0 || newFocus.getHeight() <= 0)
                return;
            Log.d(TAG, "onFocusChanged:2222222222" + oldFocus + "=" + newFocus);

            mAnimatorList.clear();

            for (FocusListener f : this.mFocusListener) {
                f.onFocusChanged(oldFocus, newFocus);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    @Override
    public void onScrollChanged(View target, View attachView) {
        try {
//            Log.d(TAG, "onScrollChanged");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onLayout(View target, View attachView) {
        try {
//            Log.d(TAG, "onLayout");
            ViewGroup viewGroup = (ViewGroup) attachView.getRootView();
            if (target.getParent() != null && target.getParent() != viewGroup) {
                target.setVisibility(View.GONE);
                if (mFirstFocus) viewGroup.requestFocus();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected boolean mFirstFocus = true;

    public void setFirstFocus(boolean b) {
        this.mFirstFocus = b;
    }

    @Override
    public void onTouchModeChanged(View target, View attachView, boolean isInTouchMode) {
        try {
            //Log.d(TAG, "onTouchModeChanged:"+isInTouchMode);
            if (mEnableTouch && isInTouchMode) {
                target.setVisibility(View.INVISIBLE);
                if (lastFocus != null) {
                    AnimatorSet animatorSet = new AnimatorSet();
                    animatorSet.playTogether(getScaleAnimator(lastFocus, false));
                    animatorSet.setDuration(0).start();
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    protected boolean isScrolling = false;

    protected List<View> attacheViews = new ArrayList<>();
    protected Map<View, AdapterView.OnItemSelectedListener> onItemSelectedListenerList = new HashMap<>();


    @Override
    public void onAttach(View target, View attachView) {
        try {
            mTarget = target;

            if (target.getParent() != null && (target.getParent() instanceof ViewGroup)) {
                ViewGroup vg = (ViewGroup) target.getParent();
                vg.removeView(target);
            }

            ViewGroup vg = (ViewGroup) attachView.getRootView();
            vg.addView(target);

            target.setVisibility(View.GONE);
            if (attachView instanceof RecyclerView) {
                RecyclerView recyclerView = (RecyclerView) attachView;
                RecyclerView.OnScrollListener recyclerViewOnScrollListener = null;
                recyclerViewOnScrollListener = new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                        try {
                            super.onScrollStateChanged(recyclerView, newState);
                            //Log.d(TAG, "========>onScrollStateChanged");

                            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                                //Log.d(TAG, "========>SCROLL_STATE_IDLE");
                                isScrolling = false;
                                View oldFocus = oldLastFocus;
                                View newFocus = lastFocus;
                                VisibleScope scope = checkVisibleScope(oldFocus, newFocus);
                                if (!scope.isVisible) {
                                    return;
                                } else {
                                    oldFocus = scope.oldFocus;
                                    newFocus = scope.newFocus;
                                }
                                AnimatorSet animatorSet = new AnimatorSet();
                                List<Animator> list = new ArrayList<>();
//                            list.addAll(getScaleAnimator(oldLastFocus, false));
                                list.addAll(getScaleAnimator(newFocus, true));
                                list.addAll(getMoveAnimator(newFocus, 0, 0));
                                animatorSet.setDuration(mDurationTranslate);
                                animatorSet.playTogether(list);
                                animatorSet.start();


                            } else if (newState == RecyclerView.SCROLL_STATE_SETTLING) {
                                //Log.d(TAG, "========>SCROLL_STATE_SETTLING=" + mAnimatorSet.isRunning());
                                isScrolling = true;
                                if (lastFocus != null) {
                                    List<Animator> list = getScaleAnimator(lastFocus, false);
                                    AnimatorSet animatorSet = new AnimatorSet();
                                    animatorSet.setDuration(150);
                                    animatorSet.playTogether(list);
                                    animatorSet.start();
                                }
                            }
                        } catch (Exception ex) {

                        }
                    }
                };
                recyclerView.addOnScrollListener(recyclerViewOnScrollListener);
            } else if (attachView instanceof AbsListView) {

                final AbsListView absListView = (AbsListView) attachView;
                final AdapterView.OnItemSelectedListener onItemSelectedListener = absListView.getOnItemSelectedListener();

                View temp = null;
                if (absListView.getChildCount() > 0) {
                    temp = absListView.getChildAt(0);
                }
                final View tempFocus = temp;
                MyOnItemSelectedListener myOnItemSelectedListener = new MyOnItemSelectedListener();
                myOnItemSelectedListener.onItemSelectedListener = onItemSelectedListener;
                myOnItemSelectedListener.oldFocus = temp;
                absListView.setOnItemSelectedListener(myOnItemSelectedListener);
                onItemSelectedListenerList.put(attachView, myOnItemSelectedListener);

            }

            attacheViews.add(attachView);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    protected class MyOnItemSelectedListener implements AdapterView.OnItemSelectedListener {
        public View oldFocus = null;
        public View newFocus = null;
        public AnimatorSet animatorSet;
        public AdapterView.OnItemSelectedListener onItemSelectedListener;

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            try {
                if (onItemSelectedListener != null && parent != null) {
                    onItemSelectedListener.onItemSelected(parent, view, position, id);
                }
                if (newFocus == null) return;
                newFocus = view;
                Log.d(TAG, "onItemSelected");

                Rect rect = new Rect();
                view.getLocalVisibleRect(rect);

                ViewGroup vg = (ViewGroup) newFocus.getParent();
//                        Log.d(TAG, "onItemSelected:" + vg.getWidth() + " " + newFocus.getMeasuredWidth() + " w=" + (rect.left - rect.right));

                int factorX = 0, factorY = 0;
                if (Math.abs(rect.left - rect.right) > newFocus.getMeasuredWidth()) {
                    factorX = (Math.abs(rect.left - rect.right) - newFocus.getMeasuredWidth()) / 2 - 1;
                    factorY = (Math.abs(rect.top - rect.bottom) - newFocus.getMeasuredHeight()) / 2;

                }


                List<Animator> animatorList = new ArrayList<Animator>(3);
                animatorList.addAll(getScaleAnimator(newFocus, true));
                if (oldFocus != null) animatorList.addAll(getScaleAnimator(oldFocus, false));
                animatorList.addAll(getMoveAnimator(newFocus, factorX, factorY));
                mTarget.setVisibility(View.VISIBLE);

                if (animatorSet != null && animatorSet.isRunning()) animatorSet.end();
                animatorSet = new AnimatorSet();
                animatorSet.setDuration(mDurationTranslate);
                animatorSet.playTogether(animatorList);
                animatorSet.start();


                oldFocus = newFocus;
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            Log.d(TAG, "onNothingSelected");
            if (onItemSelectedListener != null) {
                onItemSelectedListener.onNothingSelected(parent);
            }
        }
    }


    @Override
    public void OnDetach(View targe, View view) {
        if (targe.getParent() == view) {
            ((ViewGroup) view).removeView(targe);
        }

        attacheViews.remove(view);
    }

    @Override
    public <T> T toEffect(Class<T> t) {
        return (T) this;
    }

    public void setEnableTouch(boolean enableTouch) {
        this.mEnableTouch = enableTouch;
    }

    public boolean isScalable() {
        return mScalable;
    }

    public void setScalable(boolean scalable) {
        this.mScalable = scalable;
    }

    public float getScale() {
        return mScale;
    }

    public void setScale(float scale) {
        this.mScale = scale;
    }

    public int getMargin() {
        return mMargin;
    }

    public void setMargin(int mMargin) {
        this.mMargin = mMargin;
    }

}
