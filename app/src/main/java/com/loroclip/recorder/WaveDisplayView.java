/**
 *
 */
package com.loroclip.recorder;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.AudioFormat;
import android.os.Handler;
import android.view.View;

import java.io.FileOutputStream;
import java.util.Arrays;


public class WaveDisplayView extends View {

	private final Handler handler;

	byte[] data;
	double[] drawData;
	private int index;

	private final Paint waveBaseLine = new Paint();

	FileOutputStream out;
	String path, fileName;
	private static final int RATE = 44100;
	private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_CONFIGURATION_MONO;
	private static final int AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

	public WaveDisplayView(Context context) {
		super(context);
		handler = new Handler();
		waveBaseLine.setARGB(255, 128, 255, 128);
		waveBaseLine.setStyle(Paint.Style.STROKE);
		waveBaseLine.setStrokeWidth(1.0f);
		waveBaseLine.setStrokeCap(Paint.Cap.ROUND);

		data = new byte[0];
		index = 0;
	}

//	public void makeFile() {
//		path = Environment.getExternalStorageDirectory().toString() + "/Loroclip/";
//		fileName = new SimpleDateFormat("yyyy-MM-dd-ss").format(new Date()) + ".wav";
//		Log.d("files", String.valueOf(data.length));
//		try {
//			out = new FileOutputStream( new File(path, fileName));
//			WaveFileHeaderCreator.pushWaveHeader(out, RATE, CHANNEL_CONFIG, AUDIO_ENCODING, data.length);
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
//
//	public String getFilePath() {
//		return path + fileName;
//	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		int width = this.getWidth();
		int height = this.getHeight();


		if(data.length == 0) {
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
					double value = 0.0;
					if (plots[x][1] < 0.0) {
						isLastPlus = false;
					} else {
						value = plots[x][0];
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
		drawData[index++] = waveDataPart[waveDataPart.length - 1] * 5;

		for(int i = 1 ; i < number ; i ++) {
			waveDataPart = Arrays.copyOfRange(ds, ds.length / number * i, ds.length / number * (i + 1));
			Arrays.sort(waveDataPart);
			drawData[index++] = waveDataPart[waveDataPart.length - 1] * 5;
		}
	}

	public void setDrawData(int size) {
		drawData = new double[size];
	}
	private void drawWaveLine(Canvas canvas, double value, float x, float y, int height) {
		float nextY = height * -1 * (float)(value - 1.0) / 2.0f;
		canvas.drawLine(x, y, x, nextY, waveBaseLine);
	}

	public void addWaveData(byte[] data, int offset, int length) {

			this.data = data;
//			out.write(data, offset, length);

		fireInvalidate();
	}

	private void fireInvalidate() {
		handler.post(new Runnable() {
			@Override
			public void run() {
				WaveDisplayView.this.invalidate();
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
