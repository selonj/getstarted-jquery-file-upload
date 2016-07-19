(function () {
  $.extend($, {
    toURL: function (url) {
      if (!/^\//.test(url)) return url;
      if (config && config.baseURL) {
        return config.baseURL + url;
      }
      return url;
    }
  });
  $.ajaxPrefilter(function (options) {
    options.url = $.toURL(options.url);
  });

  $.extend($.fn, {
    when: function (condition, callback) {
      var self = this;
      if ($.isFunction(condition)) {
        condition = condition.call(self);
      }
      condition && callback.call(self);
      return self;
    },//
    findIf: function (selector, element) {
      var self = this;
      var ret = self.find(selector);
      if (ret.length)  return ret;
      ret = $($.isFunction(element) ? element() : element);
      return self.append(ret).pushStack(ret);
    }
  });

  var selectors = {
    singleton: '[data-toggle="fileupload"]',//
    multiple: '[data-toggle="multi-upload"]',//
    preview: '.fileupload-preview',//
    progress: '.fileupload-progress',//
    progressing: '.fileupload-progressing',//
    remove: '[data-toggle="remove"]'
  };

  var _progressing = function (visible, e, data) {
    if ($.type(visible) != 'boolean') {
      data = e;
      e = visible;
    }
    var progress = parseInt(data.loaded / data.total * 100, 10);
    $(this).find(selectors.progress).css('visibility', visible === false ? 'hidden' : 'visible').end().
    find(selectors.progressing).width(progress + '%');
  };

  var fileupload = window.fileupload = function (el) {
    var originalAdd = $.blueimp.fileupload.prototype.options.add;
    return $(el).fileupload({
      //      fieldName:'',
      //
      dataType: 'json',//
      add: function (e, data) {
        var type = 'upload';
        var self = $(this);
        self.dequeue(type).queue(type, function (next) {
          data.abort();
          next();
        });
        self.find(selectors.remove).one('click', function () {
          if (data.state() === 'pending') {
            self.detach();
            data.abort();
          }
        });
        originalAdd.call(this, e, data);
      },//
      send: $.proxy(_progressing, true),//
      progress: $.proxy(_progressing),//
      done: function (e, data) {
        var path = data.result.files[0], dfd = $.Deferred(), self = $(this);
        var options = self.fileupload('option');
        if (options.serviceUrl) {
          var form = {};
          form[options.serviceParamName] = path;
          $.post(options.serviceUrl, form).done(dfd.resolve).fail(dfd.reject);
        } else {
          dfd.resolve();
        }
        dfd.done(function () {
          var fieldName = options.fieldName;
          self.find(selectors.preview).findIf('img', function () {
            return $('<img>');
          }).attr('src', $.toURL(path)).end().when(fieldName, function () {
            this.findIf('input[name="' + fieldName + '"]', function () {
              return $('<input type="hidden">').attr('name', fieldName);
            }).val(path)
          });
        });
      },//
      always: $.proxy(_progressing, null, false)
    });
  };
  $(document).on('click', selectors.singleton, function (e) {
    fileupload($(e.target).closest(selectors.singleton));
  });
  $(document).on('change', selectors.multiple, function (e) {
    var file = $(e.target);
    var control = file.closest(selectors.multiple);
    var context = $($(control.attr('data-template')).html()).find('.fileupload-preview').width(control.width()).height(control.height()).end().insertAfter(control);
    window.fileupload(context).fileupload('add', {fileInput: file});
    file.val(null);
  });

})();