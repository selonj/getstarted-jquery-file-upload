var gulp = require('gulp');
var jasmineBrowser = require('gulp-jasmine-browser');

gulp.task('jasmine-phantom', function () {
  var sources = [
    'src/main/webapp/vendors/jquery/dist/jquery.js',
    'src/main/webapp/vendors/blueimp-file-upload/js/vendor/*.js',
    'src/main/webapp/vendors/blueimp-file-upload/js/jquery.fileupload.js'
  ];
  var specs = ['src/test/spec/**_spec.js'];
  return gulp.src(sources.concat(specs))
      .pipe(jasmineBrowser.specRunner({console: true}))
      .pipe(jasmineBrowser.headless());
});