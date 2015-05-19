/**
 *
 */
package com.loroclip.record.View;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.view.View;

import org.json.JSONArray;

import java.util.Arrays;


public class RecodWaveformView extends View {

	private final Handler handler;

	byte[] data;
	double[] drawData;
	private int index;

	private final Paint waveBaseLine = new Paint();

	private JSONArray mJSONArray;

	public RecodWaveformView(Context context) {
		super(context);
		handler = new Handler();
		waveBaseLine.setARGB(255, 128, 255, 128);
		waveBaseLine.setStyle(Paint.Style.STROKE);
		waveBaseLine.setStrokeWidth(1.0f);
		waveBaseLine.setStrokeCap(Paint.Cap.ROUND);

		data = null;
		index = 0;
		mJSONArray = new JSONArray();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		int width = this.getWidth();
		int height = this.getHeight();


		if(data == null) {
			return;
		}

		double[] ds = convertWaveData(data);
		setPartDrawData(16, ds);

		double[][] plots = convertPlotData(drawData, width);

		float middle = height / 2.0f;
		boolean isLastPlus = true;
		for (int x = 0; x < width; x++) {
			if(plots != null && plots[x] != null)
			{
				boolean wValue = plots[x][0] > 0.0 && plots[x][1] < 0.0;
				if (wValue) {
					double[] values = isLastPlus ? new double[] { plots[x][1], plots[x][0] } : new double[] { plots[x][0], plots[x][1] };
					for (double d : values) {
						if(d > 0.0) {
							drawWaveLine(canvas, d, x, middle, height);
							drawWaveLine(canvas, -d, x, middle, height);
						}
					}
				} else {

					if (plots[x][1] < 0.0) {
						isLastPlus = false;
					} else {
						double value = 0.001 + plots[x][0];
						isLastPlus = true;
						drawWaveLine(canvas, value, x, middle, height);
						drawWaveLine(canvas, -value, x, middle, height);
					}
				}
			}
		}
	}

	public void setPartDrawData(int number, double[] ds) {
		double[] waveDataPart;

		if (index == drawData.length) {
			for (int i = number; i < drawData.length ; i++) {
				drawData[i - number] = drawData[i];
			}
			index = index - number ;
		}

		waveDataPart = Arrays.copyOfRange(ds, 0, ds.length / number * 1);
		Arrays.sort(waveDataPart);
		drawData[index++] = waveDataPart[waveDataPart.length - 1];

		for(int i = 1 ; i < number ; i ++) {
			waveDataPart = Arrays.copyOfRange(ds, ds.length / number * i, ds.length / number * (i + 1));
			Arrays.sort(waveDataPart);
			drawData[index++] = waveDataPart[waveDataPart.length - 1];
		}
	}

	public void setDrawData(int size) {
		drawData = new double[size];
		for(int i = 0 ; i < drawData.length ; i++) {
			drawData[i] = 0;
		}
	}

	public void clearWaveData() {
		data = null;
		index = 0;
		drawData = new double[drawData.length];
//		mJSONArray = new JSONArray();
		fireInvalidate();
	}

	private void drawWaveLine(Canvas canvas, double value, float x, float y, int height) {
		float nextY = height * -1 * (float)(value - 1.0) / 2.0f;
		canvas.drawLine(x, y, x, nextY, waveBaseLine);
	}

//	private int isFirst = 0;
	public void addWaveData(byte[] data) {

		this.data = data;

		short[] shortData = new short[data.length];
		for (int index = 0; index < shortData.length; index++) {
			short d = (short) index;//((data[index * 2 + 1] << 8) + (data[index * 2] & 0xff));
			shortData[index] = d;
		}


		int mNumFrames = shortData.length / 1024;

		for (int i = 0; i < mNumFrames ; i++){
			int gain = -1;
			//getMax
			for(int j=0 ; j < 1024 ; j++) {
				int value = java.lang.Math.abs(shortData[i*1024 + j]);
				if (gain < value) {
					gain = value;
				}
			}
			mJSONArray.put((int)Math.sqrt(gain));
		}

		fireInvalidate();
	}

	public JSONArray getJsonArray(){
		return mJSONArray;
	}

	private void fireInvalidate() {
		handler.post(new Runnable() {
			@Override
			public void run() {
				RecodWaveformView.this.invalidate();
			}
		});
	}


	public static double[] convertWaveData(byte[] waveData) {
		double[] result = new double[waveData.length / 2];
		for (int index = 0; index < result.length; index++) {
			double d = (short) ((waveData[index * 2 + 1] << 8) + (waveData[index * 2] & 0xff));
			d /= Short.MAX_VALUE;
			result[index] = d;
		}
		return result;
	}

	public static double[][] convertPlotData(double[] ds, int count) {
		double[][] result = new double[count][];
		int interval = ds.length / count;
		int remainder = ds.length % count;

		int resultIndex = 0;
		for (int index = 0, counter = 0; index < ds.length; index++, counter++) {
			if (counter >= interval) {
				if (remainder > 0 && counter == interval) {
					remainder--;
				} else {
					resultIndex++;
					counter = 0;
				}
			}
			double d = ds[index];
			if (counter == 0) {
				double[] work = new double[2];
				work[d < 0 ? 1 : 0] = d;
				result[resultIndex] = work;
			} else {
				if (d >= 0 && d > result[resultIndex][0]) {
					result[resultIndex][0] = d;
				} else if (d < 0 && d < result[resultIndex][1]) {
					result[resultIndex][1] = d;
				}
			}
		}
		return result;
	}
}
