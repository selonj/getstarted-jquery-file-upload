(function () {
  var _progressing = function (visible, e, data) {
    if ($.type(visible) != 'boolean') {
      data = e;
      e = visible;
    }
    var progress = parseInt(data.loaded / data.total * 100, 10);
    $(this).find(data.selectors.progress).css('visibility', visible === false ? 'hidden' : 'visible').end().
    find(data.selectors.progressing).width(progress + '%');
  };
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

  var compSelector = '[data-toggle="fileupload"]';
  $(document).on('click', compSelector, function (e) {
    var originalAdd = $.blueimp.fileupload.prototype.options.add;
    $($(e.target).closest(compSelector)).fileupload({
      //      fieldName:'',
      selectors: {
        preview: '.fileupload-preview',//
        progress: '.fileupload-progress',//
        progressing: '.fileupload-progressing'
      },//
      dataType: 'json',//
      add: function (e, data) {
        var type = 'upload';
        $(this).dequeue(type).queue(type, function (next) {
          data.abort();
          next();
        });
        originalAdd.call(this, e, data);
      },//
      send: $.proxy(_progressing, true),//
      progress: $.proxy(_progressing),//
      done: function (e, data) {
        var path = data.result.files[0];
        var fieldName = data.fieldName;
        $(this).find(data.selectors.preview).findIf('img', function () {
          return $('<img>')
        }).attr('src', path.substring(1)).end().when(fieldName, function () {
          this.findIf('input[name="' + fieldName + '"]', function () {
            return $('<input type="hidden">').attr('name', fieldName);
          }).val(path)
        });
      },//
      always: $.proxy(_progressing, null, false)
    });
  });
})();