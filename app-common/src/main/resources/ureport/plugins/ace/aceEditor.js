/*!
 * 在线代码编辑器 基于1.4.12
 * Copyright (c) 2020-Now http://www.j2eefast.com All rights reserved.
 * No deletion without permission
 * @author ZhouHZhou
 * @date 2021年07月11日21:11:55
 * @version v1.0
 */
if (typeof jQuery === "undefined") {
    throw new Error("fastJS JavaScript requires jQuery")
}
(function($) {
    "use strict";

    // it only does '%s', and return '' when arguments are undefined
    var sprintf = function (str) {
        var args = arguments,
            flag = true,
            i = 1;

        str = str.replace(/%s/g, function () {
            var arg = args[i++];

            if (typeof arg === 'undefined') {
                flag = false;
                return '';
            }
            return arg;
        });
        return flag ? str : '';
    };

    var calculateObjectValue = function (self, name, args, defaultValue) {
        var func = name;
        if (typeof name === 'string') {
            // support obj.func1.func2
            var names = name.split('.');

            if (names.length > 1) {
                func = window;
                $.each(names, function (i, f) {
                    func = func[f];
                });
            } else {
                func = window[name];
            }
        }
        if (typeof func === 'object') {
            return func;
        }
        if (typeof func === 'function') {
            return func.apply(self, args);
        }
        if (!func && typeof name === 'string' && sprintf.apply(this, [name].concat(args))) {
            return sprintf.apply(this, [name].concat(args));
        }
        return defaultValue;
    };

    var AceEditorData = function (el, options) {
        this.options = options;
        this.$el = $(el);
        this.$el_ = this.$el.clone();
        this.data = [];
        //评论组件初始化
        this.init();
    };

    // 初始化入口
    AceEditorData.prototype.init = function () {
        //debugger;
        this.initLocale();
        this.initAce();
    };

    AceEditorData.prototype.initAce = function () {
        var that = this;
        //获取控件   id ：codeEditor
        //console.log( $('#'+that.options.id));
        //if(typeof  $('#'+that.options.id) === "undefined"){
        //     this.editor = ace.edit(that.$el);
        // }else{
        //获取初始值
        // var __per;
        // if(typeof  that.options.id == 'string'){
        //     __per = $('#'+that.options.id).html();
        //     this.editor = ace.edit($('#'+that.options.id)[0]);
        // }else{
        //     __per = that.options.id.html();
        //     this.editor = ace.edit(that.options.id[0]);
        // }

        // }
        var __per = that.$el.html();

        this.editor = ace.edit(that.$el[0]);

        var theme = "eclipse";
        //theme = "terminal";
        //语言
        var language = "ftl";
        //设置风格和语言（更多风格和语言，请到github上相应目录查看）
        this.editor.setTheme("ace/theme/" + that.options.theme);
        //语言
        this.editor.session.setMode("ace/mode/" + that.options.lang);
        //字体大小
        this.editor.setFontSize(that.options.fontSize);
        //this.editor.setTabSize(4);
        //this.editor.setHeight(that.options.height);
        //$('#'+that.options.id).css('height',that.options.height + 'px');
        // this.editor.fixedWidthGutter(false)
        //设置只读（true时只读，用于展示代码）
        this.editor.setReadOnly(that.options.readOnly);
        //自动换行,设置为off关闭
        // this.editor.setOption("wrap", "free");
        this.editor.setWrapBehavioursEnabled(false);
        this.editor.renderer.setVScrollBarAlwaysVisible(that.options.alwaysVisible);

        //启用提示菜单
        this.langTools = ace.require("ace/ext/language_tools");
        this.editor.setOptions({
            enableBasicAutocompletion: true,
            enableSnippets: true,
            enableLiveAutocompletion: true
        });

        this.editor.commands.addCommand(
            { name: 'myCommand',
                bindKey: {win: 'Ctrl-S', mac: 'Command-S'},
                exec: function(editor) {
                    if (typeof  that.options.saveAceCallback == "function"){
                        that.options.saveAceCallback(this,editor);
                    }
                }, readOnly: true // 如果不需要使用只读模式，这里设置false
            });

        if(that.options.hiddenId != ''){
            this.$hiddenId = this.$el.prev("#"+that.options.hiddenId);
            this.$hiddenId.val(that.options.base46?Base64.toBase64(__per):__per);
            $('#'+that.options.hiddenId).val(that.options.base46?Base64.toBase64(__per):__per);
            this.editor.session.on("change", function() {
                var value = that.editor.getValue();
                that.$hiddenId.val(that.options.base46?Base64.toBase64(value):value);
            });
        }

        // var addCompleter = this.editor.autoCompleter();
        // addCompleter.save(that.data);
        this.langTools.addCompleter({
            getCompletions: function(editor, session, pos, prefix, callback) {
                if (prefix.length === 0) {
                    return callback(null, []);
                } else {
                    return callback(null, that.data);
                }
            }
        });

        this.setHeight(this.options.height);
        this.setTop(this.options.top);

        //console.log(this.$el.offset().top);
        //console.log(this.$el.next('#'+this.options.id+'_toolBar').html());
        //style="position:absolute;right: 49px;top: 80px;z-index:100;"
        if(this.$el.next('#'+this.options.id+'_toolBar').length > 0){
            this.$el.append(this.$el.next('#'+this.options.id+'_toolBar'));
            // $('#'+this.options.id+'_toolBar').css({
            //     // 'position':'absolute',
            //     // 'right':'10px', //obj.offsetLeft
            //     // 'top': '0px',
            //     // 'line-height': '8px',
            //     // 'text-align': 'center',
            //     // 'float': 'left',
            //     'z-index':'1000'
            // })
        }
    };

    //本地初始化
    AceEditorData.prototype.initLocale = function () {
        var that = this;
        if (typeof ace === "undefined") {
            throw new Error("ace 对象为空,请检查是否引入!");
        }

        $.extend(this.options, $.fn.aceEditor.locales['data']);

        if(that.options.lang == 'ftl' || that.options.lang == 'freeMarker'){
            that.data = that.options.sysFtlKeydata();
            if(that.options.keywordsData &&
                that.options.keywordsData.length > 0){
                for(var i=0; i< that.options.keywordsData.length; i++){
                    var keydata = {};
                    keydata.meta = that.options.keywordsData[i].meta;
                    keydata.caption = that.options.keywordsData[i].caption;
                    keydata.value = that.options.keywordsData[i].value;
                    that.data.push(keydata);
                }
            }
        }
    };

    /**
     * 设置语言
     * @param params
     * @returns {*|boolean|void|undefined}
     */
    AceEditorData.prototype.setLang = function (lang) {
        return lang && (lang == 'java' || lang == 'ftl' || lang == 'html')
            && this.editor.session.setMode('ace/mode/'+lang);
    }

    /**
     * 设置主题
     * @param theme
     */
    AceEditorData.prototype.setTheme = function (theme) {
        theme =  theme || (theme == 'eclipse');
        this.editor.setTheme("ace/theme/" + theme);
    }

    /**
     * 设置字体
     * @param size
     */
    AceEditorData.prototype.setFontSize = function (size) {
        size =  (typeof size == "number") ? size : 14;
        this.editor.setFontSize(size);
    }

    /**
     * 设置是否可以编辑
     * @param f
     */
    AceEditorData.prototype.setReadOnly = function (f) {
        this.editor.setReadOnly(f);
    }

    /**
     * 插入数据
     * @param f
     */
    AceEditorData.prototype.insert = function (position, text) {
        this.editor.insert(position,text);
        this.editor.focus();
    }

    /**
     * 获取数据
     * @returns {*}
     */
    AceEditorData.prototype.getValue = function () {
        return this.editor.getValue();
    }

    AceEditorData.prototype.setValue = function (val) {
        this.editor.setValue(val);
        //var h = this.editor.session.getLength();
        this.editor.clearSelection();
    }

    AceEditorData.prototype.setHeight = function (height) {
        if (typeof  height != "undefined" && height) {
            //$("#"+this.options.id).height(height);
            // this.$el.height(height);
            if(height === 'auto'){
                this.$el.css("height", $('body').height() - 20);
            }else{
                this.$el.css("height",height);
            }
            this.editor.resize();
        }
    }

    AceEditorData.prototype.setTop = function (top) {
        if (typeof  top != "undefined" && top) {
            // $("#"+this.options.id).css('top',top + 'px');
            this.$el.css('top',top + 'px');
            this.editor.resize();
        }
    }


    AceEditorData.prototype.resize = function () {
        this.editor.resize();
    }

    AceEditorData.prototype.getEditor = function () {
        return this.editor;
    }

    AceEditorData.LOCALES = {};
    AceEditorData.LOCALES['data'] = AceEditorData.LOCALES.data = {
        sysFtlKeydata: function () {
            var data = [];
            data.push({meta: "javascript", caption: "script(脚本)", value: "script"})
            data.push({meta: "javascript", caption: "var", value: "var"})
            data.push({meta: "javascript", caption: "return", value: "return"})
            data.push({meta: "javascript", caption: "opt(fast.JS对象)", value: "opt"})
            data.push({meta: "javascript", caption: "opt.operate(操作处理)", value: "opt.operate"})
            data.push({meta: "javascript", caption: "opt.validate(校验封装)", value: "opt.validate"})
            data.push({meta: "javascript", caption: "opt.form(表单)", value: "opt.form"})
            data.push({meta: "javascript", caption: "opt.modal(弹出层)", value: "opt.modal"})
            data.push({meta: "javascript", caption: "opt.common(常用方法)", value: "opt.common"})
            data.push({meta: "javascript", caption: "opt.storage(本地浏览器缓存)", value: "opt.storage"})
            data.push({meta: "javascript", caption: "opt.storage(本地浏览器缓存)", value: "opt.storage"})
            return data;
        }
    };

    $.extend(AceEditorData.DEFAULTS, AceEditorData.LOCALES['data'] );

    AceEditorData.DEFAULTS = {
        id: 'aceEdit',             // 加载容器id
        theme: 'eclipse',          // 设置编辑器界面风格 默认eclipse、terminal、chrome
        lang: 'ftl',               // 编辑器设别语音
        keywordsData: [],          // 默认关键字
        fontSize: 12,              // 默认字体大小
        readOnly: true,            // 是否只读
        height: undefined,         // 高度
        top: undefined,            //
        hiddenId: '',              // 隐藏域ID
        base46: true,              // 隐藏域值是否base64转换安全提交
        alwaysVisible: false       // 横向滚动条始终可见
    }

    var allowedMethods = [
        'setLang',
        'setTheme',
        'setFontSize',
        'setReadOnly',
        'getValue',
        'setValue',
        'setHeight',
        'insert',
        'setTop',
        'resize',
        'getEditor'
    ];

    $.fn.aceEditor = function (option) {
        var value,
            args = Array.prototype.slice.call(arguments, 1);
        this.each(function () {
            var $this = $(this),
                data = $this.data('aceEditor.data'),
                options = $.extend({}, AceEditorData.DEFAULTS, $this.data(),
                    typeof option === 'object' && option);
            if (typeof option === 'string') {
                if ($.inArray(option, allowedMethods) < 0) {
                    throw new Error("Unknown method: " + option);
                }

                if (!data) {
                    return;
                }

                value = data[option].apply(data, args);

                if (option === 'destroy') {
                    $this.removeData('aceEditor.data');
                }
            }
            if (!data) {
                $this.data('aceEditor.data', (data = new AceEditorData(this, options)));
            }
        });
        return typeof value === 'undefined' ? this : value;
    };

    $.fn.aceEditor.Constructor = AceEditorData;
    $.fn.aceEditor.defaults = AceEditorData.DEFAULTS;
    $.fn.aceEditor.locales = AceEditorData.LOCALES;
    $.fn.aceEditor.utils = {
        sprintf: sprintf
    };

    $(function () {
        $('[data-toggle="aceEditor"]').aceEditor();
    });
})(jQuery);
