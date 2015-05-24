/**
 *
 */
package com.loroclip.record.View;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.view.View;

import com.loroclip.R;
import com.loroclip.model.Bookmark;

import org.json.JSONArray;
import org.json.JSONException;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;


public class RecodWaveformView extends View {

	private final Handler handler;


	private final Paint waveBaseLine;
	private Paint currentBookmarkPaint;

	private JSONArray mJSONArray;

	int measuredWidth;
	int measuredHeight;


	int numFrames;
	int[] frameGains;
	int[] mHeightsAtThisZoomLevel;

	private List<WaveformBookmarkInfomation> waveformBookmarkInfomationList;
	int makeSize;

	private final Semaphore jsonArraySemaphore;

	public RecodWaveformView(Context context) {
		super(context);
		handler = new Handler();
		waveBaseLine = new Paint();
		waveBaseLine.setAntiAlias(false);
		waveBaseLine.setColor(getResources().getColor(R.drawable.waveform_selected));

		currentBookmarkPaint = new Paint();
		waveBaseLine.setAntiAlias(false);

		initWaveformView();

		jsonArraySemaphore = new Semaphore(1);
	}

	public  void prepare() {
		try {
			jsonArraySemaphore.acquire();
			mJSONArray = new JSONArray();
			jsonArraySemaphore.release();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void initWaveformView() {
		numFrames = 0;
		waveformBookmarkInfomationList = new ArrayList<WaveformBookmarkInfomation>();
		fireInvalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if(numFrames == 0) {
			return;
		}

		measuredWidth = this.getWidth();
		measuredHeight = this.getHeight();

		int width = mHeightsAtThisZoomLevel.length;
		int ctr = measuredHeight / 2;




//	Draw waveform
		for (int i = 0; i < width; i++) {

			drawWaveformLine(canvas, i, ctr - mHeightsAtThisZoomLevel[i], ctr + 1 + mHeightsAtThisZoomLevel[i], waveBaseLine);

			for (int j = 0 ; j < waveformBookmarkInfomationList.size() ; j++) {
				WaveformBookmarkInfomation waveformBookmarkInfomation = waveformBookmarkInfomationList.get(j);
				if (
						waveformBookmarkInfomation.getStartViewIndex() <= i &&
						(waveformBookmarkInfomation.getEndViewIndex() >= i ||
						 waveformBookmarkInfomation.getEndViewIndex() < 0)) {
					currentBookmarkPaint.setColor(waveformBookmarkInfomation.getColor());
					currentBookmarkPaint.setAlpha(50);
					canvas.drawLine(i, 0, i, measuredHeight, currentBookmarkPaint);
					break;
				}
			}
		}
	}


	protected void drawWaveformLine(Canvas canvas, int x, int y0, int y1, Paint paint) {
		canvas.drawLine(x, y0, x, y1, paint);
	}

	public void setDrawData() {
		try {
			jsonArraySemaphore.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		int start;

		if(numFrames < measuredWidth / 2) {
			start = 0;
			makeSize = numFrames;
		} else {
			start = numFrames - measuredWidth / 2;
			makeSize = measuredWidth / 2;
			for(int i = 0; i < waveformBookmarkInfomationList.size(); i++) {
				WaveformBookmarkInfomation waveformBookmarkInfomation = waveformBookmarkInfomationList.get(i);
				waveformBookmarkInfomation.moveViewIndex();
				if(waveformBookmarkInfomation.getStartViewIndex() < 0 && waveformBookmarkInfomation.getEndViewIndex() < 0
						&& waveformBookmarkInfomationList.size() - 1 != i ) {
					waveformBookmarkInfomationList.remove(i);
				}
			}
		}



		frameGains = new int[makeSize];
		for(int i = start, j = 0 ; i < start + makeSize ; i++ , j++) {
			try {
				frameGains[j] = mJSONArray.getInt(i);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		// 일단 복사
		double[] smoothedGains = new double[makeSize];
		if (makeSize == 1) {
			smoothedGains[0] = frameGains[0];
		} else if (makeSize == 2) {
			smoothedGains[0] = frameGains[0];
			smoothedGains[1] = frameGains[1];
		} else if (makeSize > 2) {
			smoothedGains[0] = (double)((frameGains[0] / 2.0) + (frameGains[1] / 2.0));
			for (int i = 2; i < makeSize - 1; i++) {
				smoothedGains[i] = (double)((frameGains[i - 1] / 3.0) + (frameGains[i] / 3.0) + (frameGains[i + 1] / 3.0));
			}
			smoothedGains[makeSize - 1] = (double)((frameGains[makeSize - 2] / 2.0) + (frameGains[makeSize - 1] / 2.0));
		}

		// 최대값 찾기
		double maxGain = 1.0;
		for (int i = 0; i < makeSize; i++) {
			if (smoothedGains[i] > maxGain) {
				maxGain = smoothedGains[i];
			}
		}

		// 비율구하기
		double scaleFactor = 1.0;
		if (maxGain > 255.0) {
			scaleFactor = 255 / maxGain;
		}

		// 비율구한것 가지고 곱해서 최대값 찾기 (최대높이?)
		maxGain = 0;
		int gainHist[] = new int[256];
		for (int i = 0; i < makeSize; i++) {
			int smoothedGain = (int)(smoothedGains[i] * scaleFactor);
			if (smoothedGain < 0)
				smoothedGain = 0;
			if (smoothedGain > 255)
				smoothedGain = 255;

			if (smoothedGain > maxGain)
				maxGain = smoothedGain;

			gainHist[smoothedGain]++;
		}

		// Re-calibrate the min to be 5%
		double minGain = 0;
		int sum = 0;
		while (minGain < 255 && sum < makeSize / 20) {
			sum += gainHist[(int)minGain];
			minGain++;
		}

		// Re-calibrate the max to be 99%
		sum = 0;
		while (maxGain > 2 && sum < makeSize / 100) {
			sum += gainHist[(int)maxGain];
			maxGain--;
		}

		// Compute the heights
		double[] heights = new double[makeSize];
		double range = maxGain - minGain;
		for (int i = 0; i < makeSize; i++) {
			double value = (smoothedGains[i] * scaleFactor - minGain) / range;
			if (value < 0.0)
				value = 0.0;
			if (value > 1.0)
				value = 1.0;
			heights[i] = value * value;
		}

		int mLenByZoomLevel;
		double[] mValuesByZoomLevel;

		// Level 0 is doubled, with interpolated values
		mLenByZoomLevel = makeSize * 2;
		mValuesByZoomLevel = new double[mLenByZoomLevel];
		if (makeSize > 0) {
			mValuesByZoomLevel[0] = 0.5 * heights[0];
			mValuesByZoomLevel[1] = heights[0];
		}
		for (int i = 1; i < makeSize; i++) {
			mValuesByZoomLevel[2 * i] = 0.5 * (heights[i - 1] + heights[i]);
			mValuesByZoomLevel[2 * i + 1] = heights[i];
		}

		int halfHeight = (getMeasuredHeight() / 2) - 1;
		mHeightsAtThisZoomLevel = new int[mLenByZoomLevel];
		for (int i = 0; i < mLenByZoomLevel; i++) {
			mHeightsAtThisZoomLevel[i] = (int)(mValuesByZoomLevel[i] * halfHeight);
		}
		fireInvalidate();
	}

	public void addWaveData(byte[] data) {
		ShortBuffer shortBuffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
		int mNumSamples = shortBuffer.capacity();
		int gain, value;
		shortBuffer.rewind();

		gain = -1;
		for(int i = 0 ; i < mNumSamples; i++) {
			value = java.lang.Math.abs(shortBuffer.get());
			if (gain < value) {
				gain = value;
			}
		}
		mJSONArray.put((int) Math.sqrt(gain));
		numFrames++;

		setDrawData();
		jsonArraySemaphore.release();
	}

	// TODO 여기 무슨 작업은 해야할듯 세마포어사용???
	public JSONArray getJsonArray(){
		JSONArray result = null;
		try {
			jsonArraySemaphore.acquire();
			result = mJSONArray;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		jsonArraySemaphore.release();
		return result;
	}

	private void fireInvalidate() {
		handler.post(new Runnable() {
			@Override
			public void run() {
				RecodWaveformView.this.invalidate();
			}
		});
	}

	public void setCurrentSelectedBookmark(Bookmark selectedBookmark) {
		waveformBookmarkInfomationList.add(new WaveformBookmarkInfomation(selectedBookmark));
	}

	public void setCurrentRelaseBookmark() {
		WaveformBookmarkInfomation waveformBookmarkInfomation = waveformBookmarkInfomationList.get(waveformBookmarkInfomationList.size() - 1);
		waveformBookmarkInfomation.setEndViewIndex();
	}

	private class WaveformBookmarkInfomation{

		Bookmark bookmark;
		int startIndex;
		int endIndex;
		public WaveformBookmarkInfomation(Bookmark bookmark) {
			this.bookmark = bookmark;
			this.startIndex = makeSize * 2;
			this.endIndex = -1;
		}

		public Bookmark getBookmark() {
			return bookmark;
		}

		public void moveViewIndex() {
			startIndex -= 2;
			endIndex -= 2;
		}

		public void setEndViewIndex() {
			endIndex = makeSize * 2;
		}

		public int getStartViewIndex() {
			return startIndex;
		}

		public int getEndViewIndex() {
			return endIndex;
		}

		public int getColor() {
			return bookmark.getColor();
		}
	}
}
