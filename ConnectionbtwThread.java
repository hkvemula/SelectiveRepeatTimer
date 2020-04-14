package com.srtimer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public abstract class ConnectionbtwThread extends Thread {

	/**
	 * This method helps to move packet fast.
	 * 
	 * @param seq
	 */

	public void fast(int seq) {
		throw new UnsupportedOperationException("Not supported yet."); 
	}

	/**
	 * This method helps to move packet slow.
	 * 
	 * @param seq
	 */

	public void slow(int seq) {
		throw new UnsupportedOperationException("Not supported yet."); 
	}

	public static interface OnReceivedListener {
		void onReceived() throws Exception;
	}

	private boolean mbListening = false;

	protected void initListen(OnReceivedListener listener) {
		if (!mbListening) {
			mbListening = true;
			new Thread() {
				public void run() {
					for (;;) {
						try {
							listener.onReceived();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				};
			}.start();
		}
	}

	public void send(int seq) throws IOException {
		if (Math.random() < 0.8) {
			sendSuccess(seq);
		} else {
			fakeSend(seq);
		}
	}

	/**
	 * functioning part
	 * 
	 * @param seq
	 * @throws IOException
	 */
	protected abstract void sendSuccess(int seq) throws IOException;

	protected abstract void fakeSend(int seq) throws IOException;

	public abstract void reset(int seq) throws IOException;

	public abstract void pause(int seq) throws IOException;

	public abstract void resume(int seq) throws IOException;

	public abstract void kill(int seq) throws IOException;

	protected byte[] serialization(Serializable serializable) throws IOException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
		objectOutputStream.writeObject(serializable);
		byte[] serializedMessage = byteArrayOutputStream.toByteArray();
		objectOutputStream.close();
		byteArrayOutputStream.close();
		return serializedMessage;
	}

	protected Object deserialization(byte[] recvBytes) throws IOException, ClassNotFoundException {
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(recvBytes);
		ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
		Object deserializedObject = objectInputStream.readObject();
		objectInputStream.close();
		byteArrayInputStream.close();
		return deserializedObject;
	}
}
