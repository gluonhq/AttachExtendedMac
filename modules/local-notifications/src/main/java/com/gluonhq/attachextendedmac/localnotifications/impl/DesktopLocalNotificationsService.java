/*
 * Copyright (c) 2022, Gluon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL GLUON BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.gluonhq.attachextendedmac.localnotifications.impl;


import com.gluonhq.attachextendedmac.localnotifications.Notification;
import com.gluonhq.attach.storage.StorageService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *  Desktop implementation of LocalNotificationsService.
 */
public class DesktopLocalNotificationsService extends LocalNotificationsServiceBase {

    private static final String NOTIFICATION_IMAGE = "notificationImage.png";
    private static final Logger LOG = Logger.getLogger(LocalNotificationsServiceBase.class.getName());

    private static final String OS_NAME  = System.getProperty("os.name").toLowerCase(Locale.ROOT);

    static {
        if (OS_NAME.contains("mac")) {
            Path path = Path.of(System.getProperty("user.home"), ".gluon", "libs", "libLocalNotifications.dylib");
            if (Files.exists(path)) {
                System.load(path.toString());
                initLocalNotification();
            } else {
                LOG.log(Level.SEVERE, "Library not found at " + path);
            }
        }
    }

    private final File assetsFolder;

    public DesktopLocalNotificationsService() {
        assetsFolder = new File(StorageService.create()
                    .flatMap(StorageService::getPrivateStorage)
                    .orElseThrow(() -> new RuntimeException("Error accessing Private Storage folder")),
                "assets");

        if (!assetsFolder.exists()) {
            assetsFolder.mkdir();
        }
    }

    @Override
    protected void unscheduleNotification(Notification notification) {
        if (notification != null) {
            unregisterNotification(notification.getId());
        }
    }
    
    @Override
    protected void scheduleNotification(Notification notification) {
        createImageInAssets(notification.getImageInputStream());
        registerNotification(notification.getTitle() == null ? "" : notification.getTitle(), 
                notification.getText(), notification.getId(), notification.getDateTime().toEpochSecond());
    }

    private void createImageInAssets(InputStream imageInputStream) {
        File file = new File(assetsFolder, NOTIFICATION_IMAGE);
        if (file.exists()) {
            file.delete();
        }

        try (imageInputStream) {
            copyFile(imageInputStream, file.getAbsolutePath());
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Error reading image file", ex);
        }
    }

    private void copyFile(InputStream is, String pathEnd)  {
        if (is == null) {
            return;
        }

        try (OutputStream os = new FileOutputStream(pathEnd)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
            os.flush();
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Error copying file", ex);
        }
    }

    // native
    private native void registerNotification(String title, String text, String identifier, double seconds);
    
    private native void unregisterNotification(String identifier);
    
    private static native void initLocalNotification();

}
