package com.srtimer;

import java.awt.event.MouseAdapter;

public class PacketUtil {
	private int mY;
	private boolean mbAck;
	private boolean mbVisible;
	private boolean Selected;

	public PacketUtil(int y, boolean isAck, boolean visible) {
		mY = y;
		mbAck = isAck;
		mbVisible = visible;
		Selected = false;
	}

	public int getY() {
		return mY;
	}

	public void setY(int y) {
		mY = y;
	}

	public boolean isAck() {
		return mbAck;
	}

	public void setAck(boolean isAck) {
		mbAck = isAck;
	}

	public boolean isSelected() {
		return Selected;
	}

	public void setSelected(boolean selected) {
		Selected = selected;
	}

	public boolean isVisible() {
		return mbVisible;
	}

	public void setVisible(boolean visible) {
		mbVisible = visible;
	}

	/**
	 * It helps to listen the hovered mouse actions.
	 * 
	 * @param mouseAdapter
	 */
	public void addMouseListener(MouseAdapter mouseAdapter) {
		throw new UnsupportedOperationException("Not supported yet."); 
																
	}

}
