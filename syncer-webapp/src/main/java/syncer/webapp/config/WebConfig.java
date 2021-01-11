// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// See the License for the specific language governing permissions and
// limitations under the License.

package syncer.webapp.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import syncer.webapp.filter.LogOutFilter;
import syncer.webapp.filter.UserFilter;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/8/6
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {


    @Autowired
    private UserFilter userFilter;
    @Autowired
    private LogOutFilter logOutFilter;

    // 这个方法是用来配置静态资源的，比如html，js，css，等等
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
    }

    // 这个方法用来注册拦截器，我们自己写好的拦截器需要通过这里添加注册才能生效
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // addPathPatterns("/**") 表示拦截所有的请求，
//         excludePathPatterns("/login", "/register")
        //表示除了登陆与注册之外，因为登陆注册不需要登陆也可以访问
        registry.addInterceptor(userFilter).addPathPatterns("/**").excludePathPatterns("/login","/logout");
        registry.addInterceptor(logOutFilter).addPathPatterns("/logout");
    }

}