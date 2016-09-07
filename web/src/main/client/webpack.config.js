var webpack = require('webpack');

module.exports = {
    devtool: 'source-map',
    entry: './src/main.js',
    output: {
        path: __dirname + '/../resources/webroot/private',
        filename: 'bundle.js',
        publicPath: '/private/'
    },
    module: {
        loaders: [{
            test: /\.js$/,
            loaders: ['babel'],
            exclude: /node_modules/
        }]
    }
};
