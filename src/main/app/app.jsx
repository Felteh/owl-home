import React from 'react';
import ReactDom from 'react-dom';
import injectTapEventPlugin from 'react-tap-event-plugin';
import {App} from './main';
import {VideoListingPage} from './pages/video/video';
import {LightListingPage} from './pages/light/light';
import {browserHistory, IndexRouter, Route, Router} from "react-router-dom";

// Needed for onTouchTap
// http://stackoverflow.com/a/34015469/988941
injectTapEventPlugin();

// Render the main app react component into the app div.
// For more details see: https://facebook.github.io/react/docs/top-level-api.html#react.render
ReactDom.render(
        <Router history={browserHistory}>
            <Route path="/" component={App}>
                <Route path="/pages/video" component={VideoListingPage}/>
                <Route path="/pages/light" component={LightListingPage}/>
            </Route>
        </Router>,
        document.getElementById("app")
        );