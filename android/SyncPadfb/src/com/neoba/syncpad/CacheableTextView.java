package com.neoba.syncpad;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.Layout;
import android.util.AttributeSet;
import android.widget.TextView;

public class CacheableTextView extends TextView {
	private Bitmap mCache;
	private final Paint mCachePaint = new Paint();
	private final Canvas mCacheCanvas = new Canvas();
	private int mPrevAlpha = -1;
	private boolean mIsBuildingCache;
	boolean mWaitingToGenerateCache;
	float mTextCacheLeft;
	float mTextCacheTop;
	float mTextCacheScrollX;
	float mRectLeft, mRectTop;
	private float mPaddingH = 0;
	private float mPaddingV = 0;
	private CharSequence mText;

	public CacheableTextView(Context context) {
		super(context);
	}

	public CacheableTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CacheableTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	protected int getCacheTopPadding() {
		return 0;
	}

	protected int getCacheLeftPadding() {
		return 0;
	}

	protected int getCacheRightPadding() {
		return 0;
	}

	protected int getCacheBottomPadding() {
		return 0;
	}

	public void buildAndEnableCache() {
		// Defers building the cache until the next draw to allow measuring
		// and laying out.
		buildAndEnableCache(false);
	}

	public void buildAndEnableCache(boolean isImmediate) {
		if (getLayout() == null || !isImmediate) {
			mWaitingToGenerateCache = true;
			return;
		}
		final Layout layout = getLayout();
		final int left = getCompoundPaddingLeft();
		final int top = getExtendedPaddingTop();
		final float prevAlpha = getAlpha();
		mTextCacheLeft = layout.getLineLeft(0) - getCacheLeftPadding();
		mTextCacheTop = top + layout.getLineTop(0) - mPaddingV
				- getCacheTopPadding();
		int mScrollX=0;
		mRectLeft = mScrollX + getLeft();
		mRectTop = 0;
		mTextCacheScrollX = mScrollX;
		int mRight=0;
		int mLeft=0;
		final float textCacheRight = Math.min(left + layout.getLineRight(0)
				+ mPaddingH, mScrollX + mRight - mLeft)
				+ getCacheRightPadding();
		final float textCacheBottom = top + layout.getLineBottom(0) + mPaddingV
				+ getCacheBottomPadding();
		final float xCharWidth = getPaint().measureText("x");
		int width = (int) (textCacheRight - mTextCacheLeft + (2 * xCharWidth));
		int height = (int) (textCacheBottom - mTextCacheTop);
		if (width != 0 && height != 0) {
			if (mCache != null) {
				if (mCache.getWidth() != width || mCache.getHeight() != height) {
					mCache.recycle();
					mCache = null;
				}
			}
			if (mCache == null) {
				mCache = Bitmap.createBitmap(width, height, Config.ARGB_8888);
				mCacheCanvas.setBitmap(mCache);
			} else {
				mCacheCanvas.drawColor(0x00000000);
			}
			mCacheCanvas.save();
			mCacheCanvas.translate(-mTextCacheLeft, -mTextCacheTop);
			mIsBuildingCache = true;
			setAlpha(1.0f);
			draw(mCacheCanvas);
			setAlpha(prevAlpha);
			mIsBuildingCache = false;
			mCacheCanvas.restore();
			// A hack-- we set the text to be one space (we don't make it empty
			// just to avoid any
			// potential issues with text measurement, like line height, etc.)
			// so that the text view
			// doesn't draw it anymore, since it's been cached. We have to
			// manually rebuild
			// the cache whenever the text is changed (which is never in
			// Launcher)
			mText = getText();
			setText(" ");
		}
	}

	public CharSequence getText() {
		return (mText == null) ? super.getText() : mText;
	}

	public void draw(Canvas canvas) {
		if (mWaitingToGenerateCache && !mIsBuildingCache) {
			buildAndEnableCache(true);
			mWaitingToGenerateCache = false;
		}
		if (mCache != null && !mIsBuildingCache) {
			float mScrollX=0;
			canvas.drawBitmap(mCache, mTextCacheLeft - mTextCacheScrollX
					+ mScrollX, mTextCacheTop, mCachePaint);
		}
		super.draw(canvas);
	}

	protected boolean isBuildingCache() {
		return mIsBuildingCache;
	}

	@Override
	protected boolean onSetAlpha(int alpha) {
		if (mPrevAlpha != alpha) {
			mPrevAlpha = alpha;
			mCachePaint.setAlpha(alpha);
			super.onSetAlpha(alpha);
		}
		return true;
	}
}