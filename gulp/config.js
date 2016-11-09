var dest = './target/classes/web/',
  src = './src',
  mui = './node_modules/material-ui/src';

module.exports = {
  browserify: {
    // Enable source maps
    debug: true,
    // A separate bundle will be generated for each
    // bundle config in the list below
    bundleConfigs: [{
      entries: src + '/app/app.js',
      dest: dest,
      outputName: 'app.js'
    }],
    extensions: ['.js'],
  }
};
