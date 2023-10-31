package io.metersphere.config;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.loadbalancer.FeignLoadBalancerAutoConfiguration;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.StreamUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import feign.Feign;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;
import jakarta.servlet.http.HttpServletRequest;

/**
 * @author Punkhoo
 */
@Configuration
@ConditionalOnClass(Feign.class)
@AutoConfigureBefore(FeignLoadBalancerAutoConfiguration.class)
public class FeignConfig implements RequestInterceptor {

    @Autowired
    private ObjectFactory<HttpMessageConverters> messageConverters;

    @Bean
    public Encoder feignFormEncoder() {
        return new SpringFormEncoder(new SpringEncoder(messageConverters));
    }

    @SuppressWarnings("deprecation")
    @Bean
    public Decoder feignDecoder() {
        return new ResponseEntityDecoder(new SpringDecoder(() -> {
            MappingJackson2HttpMessageConverter fastConverter = new MappingJackson2HttpMessageConverter() {

                @Override
                public Object read(Type type, Class<?> contextClass, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
                    Object result;
                    if ("java.lang.String".equals(type.getTypeName())) {
                        result = StreamUtils.copyToString(inputMessage.getBody(), StandardCharsets.UTF_8);
                    } else {
                        result = super.read(type, contextClass, inputMessage);
                    }

                    return result;
                }
            };

            return new HttpMessageConverters(fastConverter);
        }));
    }

    @Override
    public void apply(RequestTemplate requestTemplate) {
        try {
            ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (null != servletRequestAttributes) {
                HttpServletRequest request = servletRequestAttributes.getRequest();
                // 获取所有头文件信息的key
                Enumeration<String> headerNames = request.getHeaderNames();
                if (null != headerNames) {
                    while (headerNames.hasMoreElements()) {
                        // 获取头文件的key和value
                        String headerName = headerNames.nextElement();
                        String headerValue = request.getHeader(headerName);
                        // 跳过content-length，不然可能会报too many bites written问题
                        if ("content-length".equalsIgnoreCase(headerName)) {
                            continue;
                        }
                        // 将令牌数据添加到头文件中，当用feign调用的时候，会传递给下一个微服务
                        requestTemplate.header(headerName, headerValue);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
