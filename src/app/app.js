import React from 'react';
import ReactDom from 'react-dom';
import injectTapEventPlugin from 'react-tap-event-plugin';
import {App} from './main';
import {VideoListingContainer} from './pages/video/video'
import {LightListing} from './pages/light/light'
import {Router, Route, IndexRoute, browserHistory} from 'react-router';

// Needed for onTouchTap
// http://stackoverflow.com/a/34015469/988941
injectTapEventPlugin();

// Render the main app react component into the app div.
// For more details see: https://facebook.github.io/react/docs/top-level-api.html#react.render
ReactDom.render(
    <Router history={browserHistory}>
        <Route path="/" component={App}>
            <Route path="/pages/video" component={VideoListingContainer}/>
            <Route path="/pages/light" component={LightListing}/>
        </Route>
    </Router>,
    document.getElementById("app")
);