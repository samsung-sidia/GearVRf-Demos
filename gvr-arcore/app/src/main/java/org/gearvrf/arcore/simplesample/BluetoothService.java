/* Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gearvrf.arcore.simplesample;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

class BluetoothService {
    private static final String TAG = "GVR_ARCORE";

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothAdapter mBluetoothAdapter;
    private AcceptThread mAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;

    private Handler mHandler;

    public interface MessageConstants {
        int MESSAGE_READ = 0;
        int MESSAGE_WRITE = 1;
        int MESSAGE_TOAST = 2;
        int CONNECTED = 3;
    }

    BluetoothService(Handler handler) {
        mHandler = handler;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    Set<BluetoothDevice> getPairedDevices() {
        return mBluetoothAdapter.getBondedDevices();
    }

    synchronized void startAccpetingRequests() {
        Log.d(TAG, "Accepting incoming requests");

        // Start the thread to listen on a BluetoothServerSocket
        if (mAcceptThread == null) {
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
        }
    }

    synchronized void connectToDevice(BluetoothDevice device) {
        Log.d(TAG, "Connect to: " + device);

        if (mConnectThread == null) {
            mConnectThread = new ConnectThread(device);
            mConnectThread.start();
        }
    }

    void write(byte[] out) {
        mConnectedThread.write(out);
    }

    private void manageConnectedSocket(BluetoothSocket socket) {
        Log.d(TAG, "Exchanging data");

        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        Message connectMsg = mHandler.obtainMessage(MessageConstants.CONNECTED,"Connected");
        connectMsg.sendToTarget();

        // stop other threads if necessary
    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mServerSocket;

        AcceptThread() {
            BluetoothServerSocket tmp = null;
            try {
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("ARCORE_SAMPLE", MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket's listen() method failed", e);
            }
            mServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket;

            while (true) {
                try {
                    socket = mServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "Socket's accept() method failed", e);
                    break;
                }

                if (socket != null) {
                    manageConnectedSocket(socket);
                    break;
                }
            }
        }

        public void cancel() {
            try {
                mServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mSocket;

        ConnectThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;

            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mSocket = tmp;
        }

        public void run() {
            mBluetoothAdapter.cancelDiscovery();

            try {
                mSocket.connect();
            } catch (IOException connectException) {
                try {
                    mSocket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }

            manageConnectedSocket(mSocket);
        }

        void cancel() {
            try {
                mSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mSocket;
        private final InputStream mInStream;
        private final OutputStream mOutStream;
        private byte[] mBuffer;

        ConnectedThread(BluetoothSocket socket) {
            mSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating output stream", e);
            }

            mInStream = tmpIn;
            mOutStream = tmpOut;
        }

        public void run() {
            mBuffer = new byte[1024];
            int numBytes;

            while (true) {
                try {
                    numBytes = mInStream.read(mBuffer);
                    Message readMsg = mHandler.obtainMessage(
                            MessageConstants.MESSAGE_READ, numBytes, -1,
                            mBuffer);
                    readMsg.sendToTarget();
                } catch (IOException e) {
                    Log.d(TAG, "Input stream was disconnected", e);
                    break;
                }
            }
        }

        void write(byte[] bytes) {
            try {
                mOutStream.write(bytes);

                Message writtenMsg = mHandler.obtainMessage(
                        MessageConstants.MESSAGE_WRITE, -1, -1, mBuffer);
                writtenMsg.sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data", e);

                Message writeErrorMsg =
                        mHandler.obtainMessage(MessageConstants.MESSAGE_TOAST);
                Bundle bundle = new Bundle();
                bundle.putString("toast",
                        "Couldn't send data to the other device");
                writeErrorMsg.setData(bundle);
                mHandler.sendMessage(writeErrorMsg);
            }
        }

        void cancel() {
            try {
                mSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }
}
