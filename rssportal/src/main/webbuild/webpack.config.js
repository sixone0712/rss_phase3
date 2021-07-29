const path = require('path');
const webpack = require('webpack');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const { CleanWebpackPlugin } = require('clean-webpack-plugin');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const banner = require('./banner');
const Dotenv = require('dotenv-webpack');
const CopyPlugin = require('copy-webpack-plugin');
const htmlWebpackInjectAttributesPlugin = require('html-webpack-inject-attributes-plugin');

module.exports = (env, options) => {
    //context: path.resolve(__dirname, '/js'),
    const config = {
        entry: ['@babel/polyfill', './js/index.js'],
        devtool: options.mode === 'production' ? 'hidden-source-map' : 'inline-source-map',
        // devtool: 'source-map',
        cache: true,
        output: {
            path: path.resolve('../resources/static'),
            filename: 'js/index.bundle.[hash].js',
            publicPath: '/rss'
        },
        mode: options.mode === 'production' ? 'production' : 'development',
        module: {
            rules: [{
                test: /\.(js|jsx)$/,
                exclude: /(node_modules)/,
                use: {
                    loader: 'babel-loader',
                    options: {
                        presets: ['@babel/preset-env', '@babel/preset-react'],
                        plugins: [
                            ["emotion"],
                            ["@babel/plugin-proposal-class-properties"],
                            ["@babel/plugin-transform-runtime",
                                {
                                    "regenerator": true
                                }
                            ],
                            ["import", {"libraryName": "antd", "libraryDirectory": "es"}, "antd"]
                        ]
                    }
                }
            }, {
                test: /\.(sa|sc|c)ss$/,
                use: [
                    options.mode === 'production'
                    ? MiniCssExtractPlugin.loader
                    : 'style-loader',
                    'css-loader',
                    'sass-loader'
                ]
            }]
        },
        plugins: [
            new webpack.BannerPlugin(banner),
            new HtmlWebpackPlugin({
                template: './public/index.html', // ���ø� ��θ� ����
                filename: 'index.html',
                inject: 'true',
                attributes: {
                    'charset': 'UTF-8'
                },

                /*
                minify: process.env.NODE_ENV === 'production' ? {
                    collapseWhitespace: true, // ��ĭ ����
                    removeComments: true, // �ּ� ����
                } : false,
                */
            }),
            new htmlWebpackInjectAttributesPlugin(),
            new CleanWebpackPlugin(),
            ...(
                options.mode === 'production'
                    ? [ new MiniCssExtractPlugin({filename: `css/[name].[hash].css?`}) ]
                    : []
            ),
            new Dotenv(),
            new CopyPlugin({
                patterns: [
                    { from: './public/favicon.ico', to: './' },
                    { from: './public/notsupport.html', to: './' },
                ],
            }),
        ]
    }

    return config;
}
