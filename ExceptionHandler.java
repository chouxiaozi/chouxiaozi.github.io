package org.whxd.web.support;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.OrderComparator;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.method.annotation.ExceptionHandlerMethodResolver;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.resource.DefaultServletHttpRequestHandler;
import org.springframework.web.util.UrlPathHelper;
import org.whxd.web.config.WebMvcConfig;

/**
 * 用来解决两个问题，
 * <p>
 * 1、每个jsp都需要写相应的jsp来对应，这个类能够在不用写对应controller的情况下，自动寻址到对应的jsp，寻址方式按照配置的
 * {@link ViewResolver}来寻找，需要记得配置这个东西。
 * <p>
 * 2、处理统一处理异常，有新的可以再添加
 * <p>
 * 实现方式是主动让spring抛出异常：
 * <p>
 * 具体思路为:1、关闭<mvc:default-servlet-Handler/>
 * 
 * @see WebMvcConfig#configureDefaultServletHandling(org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer)
 * @see DefaultServletHandlerConfigurer#enable()
 * @see DefaultServletHttpRequestHandler
 * @see AbstractUrlHandlerMapping#setDefaultHandler(Object)<p>
 *      需要注意的地方
 * @see DefaultServletHandlerConfigurer#getHandlerMapping 这个default
 *      -handler能够处理的请求路径为<code>/**</code>
 *      ,也就是说它能够处理所有spring处理不了的请求，这个default是有web容器来实现的
 *      。他牛逼到能处理所有web容器下的资源，当然请求的资源找不到时自然就404了。
 *      <p>
 *      spring加这个玩意的目的一个在于让default处理js和css、image等静态文件，
 *      另一个目的在于当有spring处理不了的请求的时候就直接扔给default来处理了
 *      这™完全是嫉妒default的能力，你牛逼你来处理。但是问题也在这里，首先default能处理所有的请求，所有在
 * @see DispatcherServlet#getHandler 的时候总能找到一个handler(所有自己写的handler处理不了的时候就把
 *      {@link DefaultServletHttpRequestHandler}
 *      扔出来)，DefaultServletHttpRequestHandler的处理方式简单明了
 * @see DefaultServletHttpRequestHandler#handleRequest
 *      直接forward，这样就完全出到spring外面去了，出异常也就没办法处理了，当然也可以通过web.xml的error-page处理，
 *      但是灵活性太差了。
 *      <p>
 *      所以这里关了容器的资源处理功能，所有资源处理交由spring来处理。静态文件可以通过mvc:resources来实现
 * @see WebMvcConfig#resourceHandlerMapping()
 *      spring也能够处理文件资源的请求。spring更强大的是他的异常处理能力
 * @see org.springframework.web.bind.annotation.ExceptionHandler
 * @see ExceptionHandlerMethodResolver 但是他也有他的局限，他只能处理按照他设计的套路
 * @see DispatcherServlet#doDispatch 来处理异常，不安套路出牌的话就没辙了，只能扔给web容器来处理。
 * @see HttpServlet#service(javax.servlet.ServletRequest,
 *      javax.servlet.ServletResponse)
 *      是可以有exception出现的。而容器对异常的处理通常简单粗暴，终于有自己露脸的机会了，直接把异常打印出来，顺便在尾部加上 Powerd by
 *      xxx。所以的好好利用spring有限的资源。
 *      <p>
 *      上面把default给关了之后，spring找不到处理请求的handler就会跑到
 * @see DispatcherServlet#noHandlerFound <p>
 *      2、把 {@link DispatcherServlet#setThrowExceptionIfNoHandlerFound(boolean)}
 *      nofound异常打开，这样在找不到handler的时候就会抛出异常，不然就直接404 no found，还是找不到请求路径对应的jsp。
 *      把抛异常打开之后，正常套路会跑到{@link DispatcherServlet#processDispatchResult}
 *      ，这时候如果在前面的处理过程中就会跑到{@link DispatcherServlet#processHandlerException}
 *      <p>
 *      3、出异常就得有异常处理器（也就是这个类）
 *      这个类的实现思路是：在出异常的时候，首先判断是否是jsp请求，然后根据请求路径用配置的ViewResolver尝试能不能找到对应的jsp页面
 *      ，如果有就返回相应的view，没有的话在做其他流程处理，比如error-page，nofound-page等等
 * 
 * 
 * 
 * @author ihoney
 *
 */
@ControllerAdvice
public class ExceptionHandler implements
		ApplicationListener<ContextRefreshedEvent> {
	private List<ViewResolver> viewResolvers;

	UrlPathHelper h = new UrlPathHelper();
	Logger logger = LoggerFactory.getLogger(getClass());

	@org.springframework.web.bind.annotation.ExceptionHandler(ServletException.class)
	public ModelAndView hand(HttpServletRequest request, ModelMap model) {
		if (request.getMethod().equalsIgnoreCase("get")) {
			String requestpath = h.getLookupPathForRequest(request);
			View v;
			for (ViewResolver resolver : viewResolvers) {
				try {
					v = resolver.resolveViewName(requestpath, null);
					if (v != null) {
						ModelAndView modelAndView = new ModelAndView(v);
						modelAndView.addAllObjects(model);
						return modelAndView;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {

		Map<String, ViewResolver> matchingBeans = BeanFactoryUtils
				.beansOfTypeIncludingAncestors(event.getApplicationContext(),
						ViewResolver.class, true, false);
		if (!matchingBeans.isEmpty()) {
			this.viewResolvers = new ArrayList<ViewResolver>(
					matchingBeans.values());
			OrderComparator.sort(this.viewResolvers);
		}
		logger.info(ToStringBuilder.reflectionToString(viewResolvers));
	}
}
