/*
 * Copyright 2017 Hippo Seven
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

package com.hippo.ehviewer;

/*
 * Created by Hippo on 1/14/2017.
 */

import android.app.Application;
import android.util.Log;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.backends.okhttp3.OkHttpImagePipelineConfigFactory;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.hippo.ehviewer.client.EhClient;
import com.hippo.ehviewer.network.ClimbOverDns;
import com.hippo.ehviewer.network.CookieRepository;
import com.hippo.ehviewer.network.UserAgentInterceptor;
import com.hippo.ehviewer.util.LazySupplier;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class App extends Application {

  private static final String LOG_TAG = App.class.getSimpleName();

  private static final String BASE_USER_AGENT =
      "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
          + "AppleWebKit/537.36 (KHTML, like Gecko) "
          + "Chrome/55.0.2883.87 "
          + "Safari/537.36";

  private static final String DB_COOKIE = "okhttp3-cookie.db";

  private LazySupplier<OkHttpClient> okHttpClientSupplier = new LazySupplier<OkHttpClient>() {
    @Override
    public OkHttpClient onGet() {
      return new OkHttpClient.Builder()
          .addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
              Request request = chain.request();
              // TODO Switch it is app settings
              Log.d(LOG_TAG, request.url().toString());
              return chain.proceed(request);
            }
          })
          .addInterceptor(new UserAgentInterceptor(getUserAgent()))
          .cookieJar(getCookieRepository())
          .dns(getClimbOverDns())
          .build();
    }
  };

  private LazySupplier<CookieRepository> cookieRepositorySupplier = new LazySupplier<CookieRepository>() {
    @Override
    public CookieRepository onGet() {
      return new CookieRepository(App.this, DB_COOKIE);
    }
  };

  private LazySupplier<ClimbOverDns> climbOverDnsSupplier = new LazySupplier<ClimbOverDns>() {
    @Override
    public ClimbOverDns onGet() {
      return new ClimbOverDns();
    }
  };

  private LazySupplier<EhClient> ehClientLazySupplier = new LazySupplier<EhClient>() {
    @Override
    public EhClient onGet() {
      return EhClient.create(App.this);
    }
  };

  @Override
  public void onCreate() {
    super.onCreate();

    ImagePipelineConfig config = OkHttpImagePipelineConfigFactory
        .newBuilder(this, getOkHttpClient())
        .build();
    Fresco.initialize(this, config);
  }

  public String getUserAgent() {
    // Add EhViewer segment to user agent
    return BASE_USER_AGENT + " " + BuildConfig.APPLICATION_NAME + "/" + BuildConfig.VERSION_NAME;
  }

  public OkHttpClient getOkHttpClient() {
    return okHttpClientSupplier.get();
  }

  public CookieRepository getCookieRepository() {
    return cookieRepositorySupplier.get();
  }

  public ClimbOverDns getClimbOverDns() {
    return climbOverDnsSupplier.get();
  }

  public EhClient getEhClient() {
    return ehClientLazySupplier.get();
  }
}