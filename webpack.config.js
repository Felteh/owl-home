var path = require('path');
var rootPath = path.join(__dirname, '../', '../');
var config = module.exports = {
    entry: {
        'app/bundle.main': './src/main/app/app.jsx',
    },
    devtool: 'source-map',
    cache: true,
    output: {
        path: __dirname,
        filename: './target/classes/static/[name].js',
    },
    module: {
        rules: [
            {
                test: /\.(js|jsx)$/,
                exclude: /(node_modules)/,
                use: {
                    loader: 'babel-loader',
                    options: {
                        presets: ['es2015', 'stage-2', 'env', 'react']
                    }
                }
            },
            {
                test: /\.css$/,
                use: [
                    {loader: 'style-loader'},
                    {loader: 'css-loader'}
                ]
            },
            {
                test: /\.(woff|woff2|eot|ttf|otf)$/,
                loader: "file-loader",
                options: {
                    name: './target/classes/static/app/[hash].[ext]',
                    publicPath: function (url) {
                        return url.replace("./target/classes/static/app/", 'app/')
                    },
                }
            },
            {
                test: /\.less$/,
                use: [{
                    loader: "style-loader"
                }, {
                    loader: "css-loader"
                }, {
                    loader: "less-loader", options: {
                        strictMath: true,
                        noIeCompat: true
                    }
                }]
            },
            {
                test: /\.scss$/,
                use: [{
                    loader: "style-loader"
                }, {
                    loader: "css-loader"
                }, {
                    loader: "sass-loader"
                }]
            },
            {
                test: /\.(gif|png|jpeg|jpg|svg|webp)$/i,
                loader: "image-webpack-loader"
            }
        ]
    },
    resolve: {
        extensions: ['.js', '.jsx']
    }
};
