/* ========================================================================
 * Bootstrap: scrollspy.js v3.3.5
 * http://getbootstrap.com/javascript/#scrollspy
 * ========================================================================
 * Copyright 2011-2015 Twitter, Inc.
 * Licensed under MIT (https://github.com/twbs/bootstrap/blob/master/LICENSE)
 * ======================================================================== */
+function ($, util, context) {
    'use strict';
    /**
     * 默认属性
     * @type {{offset: number}}
     */
    var DefaultOptions = {
        offset: 10
    }
    //版本信息
    var version = 'scrollspy_3.3.5';

    function ScrollSpy(element, options) {
        //上下文对象
        this.$body = $(document.body);
        //要监听的滚动对象
        this.$scrollElement = $(element).is(document.body) ? $(window) : $(element);
        //配置信息
        this.options = $.extend({}, DefaultOptions, options);
        //选择条件
        this.selector = (this.options.target || '') + ' .nav li > a';
        //当前激活对象
        this.activeTarget = null;
        this.scrollHeight = 0;
        this.init();
    }

    util.argument(ScrollSpy, {
            init: function () {
                this.$scrollElement.on('scroll.bs.scrollspy', $.proxy(this.process, this));
                this.refresh();
                this.process();
            },
            getScrollHeight: function () {
                return this.$scrollElement[0].scrollHeight || Math.max(this.$body[0].scrollHeight, document.documentElement.scrollHeight);
            },
            refresh: function () {
                var that = this
                var offsetMethod = 'offset'
                var offsetBase = 0
                this.offsets = []
                this.targets = []
                this.scrollHeight = this.getScrollHeight()
                if (!$.isWindow(this.$scrollElement[0])) {
                    offsetMethod = 'position'
                    offsetBase = this.$scrollElement.scrollTop()
                }

                this.$body
                    .find(this.selector)
                    .map(function () {
                        var $el = $(this)
                        var href = $el.data('target') || $el.attr('href')
                        var $href = /^#./.test(href) && $(href)
                        var returnv = ($href
                            && $href.length
                            && $href.is(':visible')
                            && [[$href[offsetMethod]().top + offsetBase, href]]) || null
                        return returnv
                    })
                    .sort(function (a, b) {
                        return a[0] - b[0]
                    })
                    .each(function () {
                        that.offsets.push(this[0])
                        that.targets.push(this[1])
                    })
            }
            ,
            process: function () {
                //滚动条滚到哪了
                var scrollTop = this.$scrollElement.scrollTop() + this.options.offset
                //获取滚动条的高度
                var scrollHeight = this.getScrollHeight()
                //计算最高滚动条
                var maxScroll = this.options.offset + scrollHeight - this.$scrollElement.height()
                var offsets = this.offsets
                var targets = this.targets
                var activeTarget = this.activeTarget
                var i
                //判断滚动元素是否长高了
                if (this.scrollHeight != scrollHeight) {
                    this.refresh()
                }

                //如果
                if (scrollTop >= maxScroll) {
                    return activeTarget != (i = targets[targets.length - 1]) && this.activate(i)
                }

                if (activeTarget && scrollTop < offsets[0]) {
                    this.activeTarget = null
                    return this.clear()
                }
                for (i = offsets.length; i--;) {
                    activeTarget != targets[i]
                    && scrollTop >= offsets[i]
                    && (offsets[i + 1] === undefined || scrollTop < offsets[i + 1])
                    && this.activate(targets[i])
                }
            },
            activate: function (target) {
                this.activeTarget = target
                this.clear()
                var selector = this.selector +
                    '[data-target="' + target + '"],' +
                    this.selector + '[href="' + target + '"]'
                var active = $(selector)
                    .parents('li')
                    .addClass('active')
                if (active.parent('.dropdown-menu').length) {
                    active = active
                        .closest('li.dropdown')
                        .addClass('active')
                }
                active.trigger('activate.bs.scrollspy')
            },
            clear: function () {
                $(this.selector)
                    .parentsUntil(this.options.target, '.active')
                    .removeClass('active')
            }
        }
    );
    context.ScrollSpy = ScrollSpy;
}
(jQuery, iUtil, this);
