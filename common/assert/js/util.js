/**
 * Created by ihoney on 2015/8/9 0009.
 * ���߷���
 */


+function ($, context) {
    var iUtil = context.iUtil || {};
    $.extend(iUtil, {
            /**
             *   ��̬���߷�����ǳcopy����
             * @static
             * @param source
             * @param target
             */
            mix: function (source, target) {
                return $.extend.apply(null, arguments);
            },
            /**
             *��̬���߷������ж��Ƿ�Ϊobject
             * @returns {Function}
             */
            isObject: function () {
                return (toString.call(null) === '[object Object]') ? function (o) {
                    return o !== null && o !== undefined && toString.call(o) === '[object Object]' && o.ownerDocument === undefined;
                } : function (o) {
                    return toString.call(o) === '[object Object]';
                }
            },
            /**
             * ��̬���߷������ж��Ƿ�Ϊ����
             * @returns {*}
             */
            isArray: function () {
                return ('isArray' in context.Array) ? context.Array.isArray : function (v) {
                    toString.call(v) === '[object Array]';
                }
            },
            /**
             * �ϲ�����
             * @param to
             * @param from
             */
            mixAttrs: function (to, from) {
                if (!from)return;
                for (var a in from) {
                    to[a] = to[a] || {};
                    iUtil.mixAttr(to[a], from[a]);
                }
            },
            mixAttr: function (attr, attrConfig) {
                if (!attrConfig)return;
                for (var p in attrConfig) {
                    if (p == 'value') {
                        if (iUtil.isObject(attrConfig[p])) {
                            attr[p] = attr[p] || {};
                            iUtil.mix(attr[p], attrConfig[p]);
                        } else if (isArray(attrConfig[p])) {
                            attr[p] = attr[p] || {};
                            attr[p] = attr[p].concat(attrConfig[p]);
                        } else {
                            attr[p] = attrConfig[p];
                        }
                    } else {
                        attr[p] = attrConfig[p];
                    }
                }
            },

            /**
             * �ж��Ƿ�Ϊ����
             */
            isFunction: function (fn) {
                return typeof(fn) === 'function';
            },
            /**
             *�̳�
             * @param target
             */
            argument: function (target) {
                if (!iUtil.isFunction(target)) {
                    return target;
                }
                for (var i = 1; i < arguments.length; i++) {
                    iUtil.mix(target.prototype, arguments[i].prototype || arguments[i]);
                }
            }
        }
    );
    context.iUtil = iUtil;
}
($, this);