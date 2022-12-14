/*
 * Copyright (c) 2016, 2022, Gluon
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
package com.gluonhq.attachextendedmac.runtimeargs.impl;

import com.gluonhq.attachextendedmac.runtimeargs.RuntimeArgsService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An implementation of the
 * {@link RuntimeArgsService RuntimeArgsService} for the
 * iOS platform. 
 */
public class DesktopRuntimeArgsService extends DefaultRuntimeArgsService {

    private static final String OS_NAME  = System.getProperty("os.name").toLowerCase(Locale.ROOT);
    private static final Logger LOG = Logger.getLogger(DesktopRuntimeArgsService.class.getName());

    static {
        if (OS_NAME.contains("mac")) {
            Path path = Path.of(System.getProperty("user.home"), ".gluon", "libs", "libRuntimeArgs.dylib");
            if (Files.exists(path)) {
                System.load(path.toString());
                initRuntimeArgs();
            } else {
                LOG.log(Level.SEVERE, "Library not found at " + path);
            }
        }
    }

    private static DesktopRuntimeArgsService instance;

    public DesktopRuntimeArgsService() {
        instance = this;
    }

    // Native
    
    private static native void initRuntimeArgs();

    // callback
    private static void processRuntimeArgs(String key, String value) {
        if (instance != null) {
            instance.fire(key, value);
        }
    }
}
