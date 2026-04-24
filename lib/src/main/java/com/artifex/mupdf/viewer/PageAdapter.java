package com.artifex.mupdf.viewer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.os.AsyncTask;

public class PageAdapter extends BaseAdapter {
	private final String APP = "MuPDF";
	private final Context mContext;
	private final MuPDFCore mCore;
	private final SparseArray<PointF> mPageSizes = new SparseArray<PointF>();
	private       Bitmap mSharedHqBm;
	private final FirstPageNotifier firstPageNotifier = new FirstPageNotifier();

	public PageAdapter(Context c, MuPDFCore core) {
		mContext = c;
		mCore = core;
	}

	public interface FirstPageRenderedListener {
		void onFirstPageRendered(Bitmap bitmap, String text, com.artifex.mupdf.fitz.BarcodeInfo[] barcodes);
	}

	/**
	 * Notifica apenas uma vez quando a primeira página for renderizada.
	 */
	public static class FirstPageNotifier {
		private FirstPageRenderedListener listener;
		private boolean notified;

		public void setListener(FirstPageRenderedListener listener) {
			this.listener = listener;
			this.notified = false;
		}

		public void notify(Bitmap bitmap, String text, com.artifex.mupdf.fitz.BarcodeInfo[] barcodes) {
			if (!notified && listener != null && bitmap != null) {
				notified = true;
				listener.onFirstPageRendered(bitmap, text, barcodes);
			}
		}
	}

	public void setFirstPageRenderedListener(FirstPageRenderedListener listener) {
		firstPageNotifier.setListener(listener);
	}

	public int getCount() {
		try {
			return mCore.countPages();
		} catch (RuntimeException e) {
			return 0;
		}
	}

	public Object getItem(int position) {
		return null;
	}

	public long getItemId(int position) {
		return 0;
	}

	public synchronized void releaseBitmaps()
	{
		//  recycle and release the shared bitmap.
		if (mSharedHqBm!=null)
			mSharedHqBm.recycle();
		mSharedHqBm = null;
	}

	public void refresh() {
		mPageSizes.clear();
	}

	public synchronized View getView(final int position, View convertView, ViewGroup parent) {
		final PageView pageView;
		if (convertView == null) {
			Point parentSize = getSafeParentSize(parent);
			if (mSharedHqBm == null || mSharedHqBm.getWidth() != parentSize.x || mSharedHqBm.getHeight() != parentSize.y)
			{
				mSharedHqBm = Bitmap.createBitmap(parentSize.x, parentSize.y, Bitmap.Config.ARGB_8888);
			}

			pageView = new PageView(mContext, mCore, parentSize, mSharedHqBm, firstPageNotifier);
		} else {
			pageView = (PageView) convertView;
			pageView.setFirstPageNotifier(firstPageNotifier);
		}

		PointF pageSize = mPageSizes.get(position);
		if (pageSize != null) {
			// We already know the page size. Set it up
			// immediately
			pageView.setPage(position, pageSize);
		} else {
			// Page size as yet unknown. Blank it for now, and
			// start a background task to find the size
			pageView.blank(position);
			AsyncTask<Void,Void,PointF> sizingTask = new AsyncTask<Void,Void,PointF>() {
				@Override
				protected PointF doInBackground(Void... arg0) {
					try {
						return mCore.getPageSize(position);
					} catch (RuntimeException e) {
						return null;
					}
				}

				@Override
				protected void onPostExecute(PointF result) {
					super.onPostExecute(result);
					// We now know the page size
					mPageSizes.put(position, result);
					// Check that this view hasn't been reused for
					// another page since we started
					if (pageView.getPage() == position)
						pageView.setPage(position, result);
				}
			};

			sizingTask.execute((Void)null);
		}
		return pageView;
	}

	private Point getSafeParentSize(ViewGroup parent) {
		int width = parent != null ? parent.getWidth() : 0;
		int height = parent != null ? parent.getHeight() : 0;
		if (width <= 0 || height <= 0) {
			DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
			if (width <= 0) {
				width = metrics.widthPixels;
			}
			if (height <= 0) {
				height = metrics.heightPixels;
			}
		}
		return new Point(Math.max(1, width), Math.max(1, height));
	}
}
