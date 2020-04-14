package com.srtimer;
import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("serial")
public class SRTimer extends JFrame {
	
	private final static int SIZE_BUFFER = 10;
	private BufferSlotTimer[] mBufferSender = new BufferSlotTimer[SIZE_BUFFER];
	private BufferSlotTimer[] mBufferReceiver = new BufferSlotTimer[SIZE_BUFFER];
	private PacketUtil[] mFlying = new PacketUtil[SIZE_BUFFER];
	
	private final static int SIZE_WINDOW = 5;
	private int mBaseSnd = 0;
	private int mNextSeqNum = 0;
	private int mBaseRcv = 0;
	
	private ConnectionbtwThread mSender;
	private ConnectionbtwThread mReciever;
	public String messagestring = "";
	public TextArea output;
	
	Button btnSnd;
	Button pauseBtn;
	Button fastSnd;
	Button slowBtn;
	Button killBtn;
	Button resetBtn;
	int seq;

	// Main Logic
	public SRTimer() throws SocketException, UnknownHostException {
		for (int i = 0; i < mBufferSender.length; ++i) {
			mBufferSender[i] = new BufferSlotTimer();
		}
		for (int i = 0; i < mBufferReceiver.length; ++i) {
			mBufferReceiver[i] = new BufferSlotTimer();
		}
		for (int i = 0; i < mFlying.length; ++i) {
			mFlying[i] = new PacketUtil(0, false, false);
		}
		mSender = new PacketSender(this); //SenderPacket Program is linked
		mReciever = new PacketReceiver(this); //ReceiverPacket Program is linked
		mSender.start();
		mReciever.start();
		
		//Packet Labeling
		setLayout(null);
		Label packetcolor = new Label("");
		packetcolor.setBounds(20, 50, 40, 20);
		packetcolor.setBackground(Color.yellow);
		packetcolor.bounds();
		add(packetcolor);
		Label label1 = new Label("Packet");
		label1.setBounds(60, 50, 60, 20);
		add(label1);
		
        //Acknowledgement Labeling
		Label ackcolor = new Label("");
		ackcolor.setBounds(120,50, 40, 20);
		ackcolor.setBackground(Color.blue);
		ackcolor.bounds();
		add(ackcolor);
		Label label2 = new Label("Ack");
		label2.setBounds(160, 50, 70, 20);
		add(label2);
		
        //Receiver Labeling
		Label receivedcolor = new Label("");
		receivedcolor.setBounds(230, 50, 40, 20);
		receivedcolor.setBackground(Color.green);
		receivedcolor.bounds();
		add(receivedcolor);
		Label label3 = new Label("Received");
		label3.setBounds(270, 50, 60, 20);
		add(label3);
		
        //Acknowledgement Received Labeling
		Label ackrcolor = new Label("");
		ackrcolor.setBounds(330, 50, 40, 20);
		ackrcolor.setBackground(Color.pink);
		ackrcolor.bounds();
		add(ackrcolor);
		Label label4 = new Label("Ack Received");
		label4.setBounds(370, 50, 90, 20);
		add(label4);
		
        //Buffered Labeling
		Label buffercolor = new Label("");
		buffercolor.setBounds(460, 50, 40, 20);
		buffercolor.setBackground(Color.gray);
		buffercolor.bounds();
		add(buffercolor);
		Label label5 = new Label("Buffered");
		label5.setBounds(500, 50, 70, 20);
		add(label5);
		
        //Selected Labeling
		Label selectcolor = new Label("");
		selectcolor.setBounds(570, 50, 40, 20);
		selectcolor.setBackground(Color.cyan);
		selectcolor.bounds();
		add(selectcolor);
		Label label6 = new Label("Selected");
		label6.setBounds(610, 50, 80, 20);
		add(label6);

		initBtnSnd();
		pauseBtn();
		fastBtn();
		slowBtn();
		killBtn();
		resetBtn();
		setSize(1024, 600);
		setBackground(Color.WHITE);
		setTitle("SRTimer");
    /**
     * Text box where we can see content about the packets sent between sender and receiver.
     */
		output = new TextArea(150, 150); // setting up the output box
		//creating text area for consoling output box
		output.setBounds(650, 100, 350, 300); // setting bounds for output box
		output.setEditable(false); //preventing user from editing the written output.
		// Console
		add(output);
		output.append("---- Press SEND PACKET button to start ---- \n");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
	}

	/**
	 * BtnSnd is used to send the packet to receiver from sender.
	 */
	private void initBtnSnd() {
		final int x = OFFSET_X_SENDER_SLOT - 20;
		final int y = OFFSET_Y_SENDER_SLOT - 110;
		btnSnd = new Button("Send Packet");
		btnSnd.setBounds(x, y, 80, 40);
		btnSnd.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				seq = getSeqSndNew();

				if (seq == -1) {
					return;
				}
				try {
					output.append("(S) - Timer started for Packet " + mNextSeqNum + "\n");
					mSender.send(seq);
				} catch (IOException e1) {
					Logger.getLogger(SRTimer.class.getName()).log(Level.SEVERE, null, e1);
				}
			}
		});
		this.add(btnSnd);
	}
	
	/**
	 * PauseBtn is used to pause the process.
	 */
	private void pauseBtn() {
		final int x = OFFSET_X_SENDER_SLOT + 80;
		final int y = OFFSET_Y_SENDER_SLOT - 110;
		pauseBtn = new Button("Pause");
		pauseBtn.setBounds(x, y, 80, 40);
		pauseBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (pauseBtn.getLabel().equals("Pause")) {
					try {
						mSender.pause(seq);
						mReciever.pause(seq);
						pauseBtn.setLabel("Pause");
						killBtn.setEnabled(true);
						btnSnd.setEnabled(false);
						fastSnd.setEnabled(false);
						slowBtn.setEnabled(false);
						resetBtn.setEnabled(false);
					} catch (IOException ex) {
						Logger.getLogger(SRTimer.class.getName()).log(Level.SEVERE, null, ex);
					}
					output.append("---- Packet Paused \n");
				} else {
					try {
						mSender.pause(seq);
						pauseBtn.setLabel("Pause");
						killBtn.setEnabled(false);
						btnSnd.setEnabled(true);
						fastSnd.setEnabled(true);
						slowBtn.setEnabled(true);
						resetBtn.setEnabled(true);
					} catch (IOException ex) {
						Logger.getLogger(SRTimer.class.getName()).log(Level.SEVERE, null, ex);
					}
				}
			}

			});
		this.add(pauseBtn);
	}

	/**
	 * FastBtn is used to fasten the process.
	 */
	private void fastBtn() {
		final int x = OFFSET_X_SENDER_SLOT + 180;
		final int y = OFFSET_Y_SENDER_SLOT - 110;
		fastSnd = new Button("Fast");
		fastSnd.setBounds(x, y, 80, 40);
		fastSnd.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println(seq);
				for (int i = 0; i < mBufferSender.length; ++i) {
					if (i == seq) {
						output.append("---- FAST mode ON ---- \n");
						mSender.fast(seq);
					}
				}
			}
		});
		this.add(fastSnd);
	}
    
	/**
	 * SlowBtn is used to slower the process.
	 */
	private void slowBtn() {
		final int x = OFFSET_X_SENDER_SLOT + 280;
		final int y = OFFSET_Y_SENDER_SLOT - 110;
		slowBtn = new Button("Slow");
		slowBtn.setBounds(x, y, 80, 40);
		slowBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				output.append("---- SLOW mode ON ---- \n");
				mSender.slow(seq);
			}
		});
		this.add(slowBtn);
	}

	/**
	 * KillBtn is used to kill the packet.
	 */
	public void killBtn() {
		final int x = OFFSET_X_SENDER_SLOT + 380;
		final int y = OFFSET_Y_SENDER_SLOT - 110;
		killBtn = new Button("Kill Pkt/Ack");
		killBtn.setBounds(x, y, 80, 40);
		killBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (pauseBtn.getLabel().equals("Pause")) {
					pauseBtn.setLabel("Pause");
					killBtn.setEnabled(false);
					btnSnd.setEnabled(true);
					fastSnd.setEnabled(true);
					slowBtn.setEnabled(true);
					resetBtn.setEnabled(true);
				}
				try {
					mSender.kill(seq);
					mSender.pause(seq);
					mReciever.kill(seq);
					mBufferReceiver[seq].startTimerTimeout(null);
				} catch (IOException ex) {
					Logger.getLogger(SRTimer.class.getName()).log(Level.SEVERE, null, ex);
				}

			}
		});
		killBtn.setEnabled(false);
		this.add(killBtn);
	}

	/**
	 * ResetBtn is used to reset the changes done in the SRTimer displays the text to start send packet again.
	 */
	private void resetBtn() {
		final int x = OFFSET_X_SENDER_SLOT + 480;
		final int y = OFFSET_Y_SENDER_SLOT - 110;
		resetBtn = new Button("Reset");
		resetBtn.setBounds(x, y, 80, 40);
		resetBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int seq = getSeqSndNew();
				try {
					mSender.reset(seq);
				} catch (IOException ex) {
					Logger.getLogger(SRTimer.class.getName()).log(Level.SEVERE, null, ex);
				}
				try {
					mReciever.reset(seq);
				} catch (IOException ex) {
					Logger.getLogger(SRTimer.class.getName()).log(Level.SEVERE, null, ex);
				}
				// TODO Auto-generated method stub
				for (int i = 0; i < mBufferReceiver.length; ++i) {
					mBufferReceiver[i] = new BufferSlotTimer();
				}
				for (int i = 0; i < mBufferSender.length; ++i) {
					mBufferSender[i] = new BufferSlotTimer();
				}
				for (int i = 0; i < mFlying.length; ++i) {
					mFlying[i] = new PacketUtil(0, false, false);
				}

				mBaseSnd = 0;
				mNextSeqNum = 0;
				output.append("---- Click on SEND PACKET button to start ---- \n");
			}
		});
		this.add(resetBtn);
	}

	public void updateBaseRcv() {
		int pivot = findFirstEmptySlotFromBufferRcv();
		for (int i = 0; i < mBufferReceiver.length; ++i) {
			if (i < pivot) {
				mBufferReceiver[i].setState(BufferSlotTimer.RECEIVED);
				if (i < mBufferReceiver.length - SIZE_WINDOW) {
					mBaseRcv = i + 1;
				}
			}
		}
	}

	//finding the NextSequence
	private int findFirstEmptySlotFromBufferRcv() {
		int i;
		for (i = 0; i < mBufferReceiver.length; ++i) {
			if (mBufferReceiver[i].getState() == BufferSlotTimer.EMPTY) {
				return i;
			}
		}
		return i + 1;
	}

	public void updateBaseSnd() {
		mBaseSnd = findBaseSnd();
	}

	private int findBaseSnd() {
		int i;
		for (i = 0; i < mBufferSender.length - SIZE_WINDOW; ++i) {
			if (mBufferSender[i].getState() != BufferSlotTimer.ACKED) {
				return i;
			}
		}
		return i;
	}

	private int getSeqSndNew() {
		for (int i = mBaseSnd; i < mBaseSnd + SIZE_WINDOW; ++i) {
			if (mBufferSender[i].getState() == BufferSlotTimer.EMPTY) {
				return i;
			}
		}
		return -1;
	}

	public void updateNextSequenceSnd() {
		mNextSeqNum = findNextSequenceSnd();
	}

	private int findNextSequenceSnd() {
		int i;
		for (i = 0; i < mBufferSender.length; ++i) {
			if (mBufferSender[i].getState() == BufferSlotTimer.EMPTY) {
				return i;
			}
		}
		return i;
	}
    /**
     * Template dimensions
     */
	public static final int OFFSET_X_SENDER_SLOT = 50;
	public static final int OFFSET_Y_SENDER_SLOT = 200;
	public static final int MARGIN_SLOT = 10;
	public static final int WIDTH_SLOT = 20;
	public static final int HEIGHT_SLOT = 50;
	public static final int DISTANCE_BETWEEN_BUFFERS = 200;
	public static final int OFFSET_X_RECEIVER_SLOT = OFFSET_X_SENDER_SLOT;
	public static final int OFFSET_Y_RECEIVER_SLOT = OFFSET_Y_SENDER_SLOT + DISTANCE_BETWEEN_BUFFERS;
	public static final int DISTANCE_TO_TEXT = 50;

	/**
	 * Sender and Receiver Graphics
	 */
	public void paint(Graphics g) {
		super.paint(g);
		Image image = createImage(getSize().width, getSize().height + 100);
		Graphics graphics = image.getGraphics();

		drawBufferSender(graphics, OFFSET_X_SENDER_SLOT, OFFSET_Y_SENDER_SLOT, DISTANCE_TO_TEXT, MARGIN_SLOT,
				WIDTH_SLOT, HEIGHT_SLOT);
		drawBufferReceiver(graphics, OFFSET_X_RECEIVER_SLOT, OFFSET_Y_RECEIVER_SLOT, DISTANCE_TO_TEXT, MARGIN_SLOT,
				WIDTH_SLOT, HEIGHT_SLOT);
		drawWindow(graphics, mBaseSnd, OFFSET_X_SENDER_SLOT, OFFSET_Y_SENDER_SLOT, MARGIN_SLOT, WIDTH_SLOT,
				HEIGHT_SLOT);
		drawWindow(graphics, mBaseRcv, OFFSET_X_RECEIVER_SLOT, OFFSET_Y_RECEIVER_SLOT, MARGIN_SLOT, WIDTH_SLOT,
				HEIGHT_SLOT);
		drawFlyings(graphics, OFFSET_X_SENDER_SLOT, OFFSET_Y_SENDER_SLOT, MARGIN_SLOT, WIDTH_SLOT, HEIGHT_SLOT);
		g.drawImage(image, 0, 0, this);
	}

	private void drawFlyings(Graphics g, int offsetBufferSenderX, int offsetBufferSenderY, int maginSlot, int widthSlot,
			int heightSlot) {
		int x = 0;
		int y = 0;
		for (int i = 0; i < mFlying.length; ++i) {
			if (!mFlying[i].isVisible()) {
				continue;
			}
			x = offsetBufferSenderX + ((maginSlot + widthSlot) * i);
			y = offsetBufferSenderY + mFlying[i].getY();
			g.setColor(Color.BLACK);
			g.draw3DRect(x, y, widthSlot, heightSlot, true);
			if (mFlying[i].isAck()) {
				g.setColor(Color.BLUE);
				g.fill3DRect(x, y, widthSlot, heightSlot, true);
			} else if (!mFlying[i].isAck()) {
				g.setColor(Color.YELLOW);
				g.fill3DRect(x, y, widthSlot, heightSlot, true);
			} else {
				System.out.println("Nothing Happened");
			} 
			if (mFlying[i].isSelected()) {
				g.setColor(Color.CYAN);
				g.fill3DRect(x, y, widthSlot, heightSlot, true);
			}
		} 
	} 

	private void drawBufferSender(Graphics g, int offsetBufferX, int offsetBufferY, int distToText, int maginSlot,
			int widthSlot, int heightSlot) {
		int x = 0;
		int y = 0;
		g.setFont(new Font(g.getFont().getName(), Font.PLAIN, g.getFont().getSize()));
		for (int i = 0; i < mBufferSender.length; ++i) {
			x = offsetBufferX + ((maginSlot + widthSlot) * i);
			y = offsetBufferY;
			g.setColor(Color.BLACK);
			g.drawString("" + i, x, y - 10);
			g.draw3DRect(x, y, widthSlot, heightSlot, true);
			switch (mBufferSender[i].getState()) {
			case BufferSlotTimer.SENT:
				g.setColor(Color.YELLOW);
				g.fill3DRect(x, y, widthSlot, heightSlot, true);
				break;
			case BufferSlotTimer.ACKED:
				g.setColor(Color.PINK);
				g.fill3DRect(x, y, widthSlot, heightSlot, true);
				break;
			}
		}
		g.setColor(Color.black);
		g.setFont(new Font(g.getFont().getName(), Font.BOLD, g.getFont().getSize()));
		g.drawString("Sender", x + distToText, y);
		g.setColor(Color.BLACK);
		g.setFont(new Font(g.getFont().getName(), Font.PLAIN, g.getFont().getSize()));
		g.drawString("Window size: " + SIZE_WINDOW, x + distToText, y + 15);
		g.drawString("base: " + mBaseSnd, x + distToText, y + 30);
		g.drawString("nextseqnum: " + mNextSeqNum, x + distToText, y + 45);
	}

	private void drawBufferReceiver(Graphics g, int offsetBufferX, int offsetBufferY, int distToText, int maginSlot,
			int widthSlot, int heightSlot) {
		int x = 0;
		int y = 0;
		g.setFont(new Font(g.getFont().getName(), Font.PLAIN, g.getFont().getSize()));
		for (int i = 0; i < mBufferSender.length; ++i) {
			x = offsetBufferX + ((maginSlot + widthSlot) * i);
			y = offsetBufferY;
			g.setColor(Color.BLACK);
			g.drawString("" + i, x, y - 10);
			g.draw3DRect(x, y, widthSlot, heightSlot, true);
			switch (mBufferReceiver[i].getState()) {
			case BufferSlotTimer.BUFFERED:
				g.setColor(Color.GRAY);
				g.fill3DRect(x, y, widthSlot, heightSlot, true);
				break;
			case BufferSlotTimer.RECEIVED:
				g.setColor(Color.GREEN);
				g.fill3DRect(x, y, widthSlot, heightSlot, true);
				break;
			}
		}
		g.setFont(new Font(g.getFont().getName(), Font.BOLD, g.getFont().getSize()));
		g.drawString("Receiver", x + distToText, y);
		g.setColor(Color.BLACK);
		g.setFont(new Font(g.getFont().getName(), Font.PLAIN, g.getFont().getSize()));
		g.drawString("Send Window size: " + SIZE_WINDOW, x + distToText, y + 15);
	}

	private void drawWindow(Graphics g, int base, int offsetBufferX, int offsetBufferY, int marginSlot, int widthSlot,
			int heightSlot) {
		final int MARGIN_WINDOW = 10;
		g.setColor(Color.BLACK);
		g.drawRect(offsetBufferX + ((marginSlot + widthSlot) * base) - MARGIN_WINDOW, offsetBufferY - MARGIN_WINDOW,
				((marginSlot + widthSlot) * SIZE_WINDOW) + MARGIN_WINDOW, heightSlot + (MARGIN_WINDOW * 2));
	}

	// Getting and Updating the buffer slots for Sender,Receiver and Flying of packets between them.
	public BufferSlotTimer getBufferSlotTimerSender(int seq) {
		return mBufferSender[seq];
	}

	public void updateBufferSlotTimerSender(int seq, int state) {
		mBufferSender[seq].setState(state);

	}

	public BufferSlotTimer getBufferSlotTimerReceiver(int seq) {
		return mBufferReceiver[seq];
	}

	public void updateBufferSlotTimerReceiver(int seq, int state) {
		mBufferReceiver[seq].setState(state);
	}

	public PacketUtil getFlyingPart(int seq) {
		return mFlying[seq];
	}

	public void retransmit() throws IOException {
			for (int i = 0; i < mBufferSender.length; ++i) {
			if (mBufferSender[i].getState() == BufferSlotTimer.SENT) {
				mBufferSender[i].stopTimerTimeout();
				if (!mFlying[i].isVisible()) {
					mSender.send(i);
				}
			} 
		}
	}

	public static void main(String[] args) throws SocketException, UnknownHostException {
		new SRTimer();
	}

} 
