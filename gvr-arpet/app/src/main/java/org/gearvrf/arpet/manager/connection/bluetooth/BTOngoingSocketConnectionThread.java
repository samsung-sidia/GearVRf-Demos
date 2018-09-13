/*
 * Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.gearvrf.arpet.manager.connection.bluetooth;

import android.support.annotation.NonNull;

import org.gearvrf.arpet.connection.OnConnectionListener;
import org.gearvrf.arpet.connection.OnMessageListener;
import org.gearvrf.arpet.connection.socket.OngoingSocketConnectionThread;

public class BTOngoingSocketConnectionThread extends OngoingSocketConnectionThread {

    BTOngoingSocketConnectionThread(
            @NonNull BTSocket socket,
            @NonNull OnMessageListener messageListener,
            @NonNull OnConnectionListener connectionListener) {

        super(socket, messageListener, connectionListener);
    }
}