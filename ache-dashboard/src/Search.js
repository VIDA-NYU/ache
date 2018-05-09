import React from 'react';
import {
  SearchkitManager, SearchkitProvider, SearchBox, Hits, RefinementListFilter,
  ActionBar, ActionBarRow, HitsStats, ViewSwitcherToggle, SelectedFilters,
  ResetFilters, Pagination
} from "searchkit";
import {without} from "lodash";
import URLUtils from './URLUtils';

import {ACHE_API_ADDRESS} from './Config';
import {api} from './RestApi';

var searchkitProps;
if(api.authorization !== undefined) {
  searchkitProps = {
    httpHeaders: {'Authorization': api.authorization}
  };
}

class LabelsManager {

  constructor(crawlerId) {
    this.crawlerId = crawlerId;
    this.listeners = [];
    this.labelsCache = {};
    api.get('/crawls/' + this.crawlerId + '/labels')
       .then(this.updateLabelsCache.bind(this));
  }

  /*
   * Register a callback function that is called any time that labels change.
   * It returns a function that can be used to unregister the listener added by
   * calling it. For example:
   *
   *   let stopListening = lm.addListener(fn);
   *   stopListening(); // calling this will remove fn fuction from listeners
   *
   */
  addListener(fn) {
    this.listeners.push(fn);
    return () => {
      this.listeners = without(this.listeners, fn);
    }
  }

  notifyListeners(changedLabels){
    this.listeners.forEach((fn) => {
     fn.apply(changedLabels)
    });
  }

  updateLabelsCache(response) {
      if(response === "FETCH_ERROR") {
        console.log("Failed to fetch labels from server.");
        return;
      }
      this.labelsCache = response;
      this.notifyListeners(this.labelsCache);
  }

  sendLabels(labels, callback) {
    let config = {
      headers: {'Content-Type': 'application/json'},
      body: JSON.stringify(labels)
    };
    api.put('/crawls/' + this.crawlerId + '/labels', config)
       .then(this.updateLabelsCache.bind(this))
       .then(callback);
  }

  isRelevant(url) {
    return this.labelsCache[url] === true;
  }

  isIrrelevant(url) {
    return this.labelsCache[url] === false;
  }

}

class HitItem extends React.Component {

  constructor(props) {
    super(props);
    this.labelsManager = props.labelsManager;
    // forces re-render every time a label changes
    this.stopListeningLabelChanges = this.labelsManager.addListener(()=> this.setState({}));
  }

  componentWillUnmount() {
    this.stopListeningLabelChanges();
  }

  formatDate(timestamp) {
    var dateObject = new Date(timestamp);
    var YYYY,MM,M,DD,D,hh,h,mm,m,ss,s;
    YYYY = dateObject.getFullYear();
    M = dateObject.getMonth()+1
    MM = M < 10 ? ('0'+M) : M;
    D = dateObject.getDate()
    DD = D < 10 ? ('0'+D) : D;
    h = dateObject.getHours();
    if (h===0) h=24;
    if (h>12) h-=12;
    hh = h<10?('0'+h):h;
    m = dateObject.getMinutes();
    mm = m < 10 ? ('0'+m) : m;
    s = dateObject.getSeconds();
    ss = s < 10 ? ('0'+s) : s;
    return YYYY+"-"+MM+"-"+DD+" "+hh+":"+mm+":"+ss;
  }

  extractDescription(input) {
    // try to extraction description from metatag og:description
    var clean = '[No Description Available]';
    if(input.text !== null) {
      clean = input.text;
    }
    if(input.html !== null) {
      var ogdesc = input.html.match(/<meta property="og:description" content="(.*?)"/i);
      if(ogdesc !== null) {
        clean = ogdesc[1] + ' || ' + clean;
      }
    }
    clean = clean.replace(/\\n/g, " ");
    clean = clean.replace(/\s\s+/g, ' ' );
    let maxlimit = 350;
    return (clean.length > maxlimit) ? (clean.substring(0,maxlimit-3) + '...') : clean;
  }

  extractImageFromSource(input) {
    if(input.html === null) {
      return '';
    }
    var html = input.html;
    // try to extract og:image or the first <img> tag available in the html
    var result = html.match(/<meta property="og:image" content="(.*?)"/i);
    if(result === null) {
      result = html.match(/<img [ˆ><]*src="(.*?)"/i);
    }

    if(result === null) {
      // could not find any image
      return '';
    } else {
      // could find a image
      var img_url = result[1].replace(/&amp;/g,"&"); // clean html entities if found
      // try to fix or resolve relative URLs
      if(img_url.indexOf('http://') === 0 ||
         img_url.indexOf('https://') === 0) { // complete URL found
        return img_url;
      }
      if(img_url.indexOf('//') === 0) { // URL without protocol found
        return 'http:'+img_url;
      }
      // relative URL found
      return new URLUtils(img_url, input.url).href;
    }
  }

  labelAs(url, feedback) {
    var domainLabels = {};
    domainLabels[url] = feedback;
    this.labelsManager.sendLabels(domainLabels);
  }

  labelAsRelevant(result) {
    this.labelAs(result._source.url, true);
  }

  labelAsIrrelevant(result) {
    this.labelAs(result._source.url, false);
  }

  render() {
    const props = this.props;
    const source = props.result._source;
    const pageDesc = this.extractDescription(source);
    const pageTitle = source.title !== null ? source.title : '[No Title Available]';
    const labeldAsRelevant = this.labelsManager.isRelevant(source.url);
    const labeldAsIrrelevant = this.labelsManager.isIrrelevant(source.url);

    return (
      <div className="row hit-item">
        <div className="col-sm-12">
          <div className="hit-title">
            <a href={source.url} target="_blank" rel="noopener noreferrer">{pageTitle}</a>
          </div>
          <div className="hit-url">
            <a href={source.url} target="_blank" rel="noopener noreferrer">{source.url}</a>
          </div>
          <div className="row">
            <div className="col-sm-2 hit-image">
              <img src={this.extractImageFromSource(source)} alt="" referrerPolicy="no-referrer" />
            </div>
            <div className="col-sm-10">
              <div className="hit-description">{pageDesc}</div>
              <ul className="list-inline hit-properties">
                <li><b>Crawl time:</b> <span className="label label-default">{this.formatDate(source.retrieved)}</span></li>
                <li><b>Classified as:</b> <span className="label label-default">{source.isRelevant}</span></li>
                <li>
                  <b>Actual label:</b>
                  <button onClick={()=>this.labelAsRelevant(props.result)}>
                    <span className={"glyphicon glyphicon-thumbs-up"  + (labeldAsRelevant ? ' relevant' : '')}></span>
                  </button>
                  <button onClick={()=>this.labelAsIrrelevant(props.result)}>
                    <span className={"glyphicon glyphicon-thumbs-down"  + (labeldAsIrrelevant ? ' irrelevant' : '')}></span>
                  </button>
                </li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    )
  }
}

class LabelAllButtons extends React.Component {

  constructor(props) {
    super(props);
    this.labelsManager = props.labelsManager;
    this.stopListeningResults = this.props.searchkit.addResultsListener(this.updateResults.bind(this));
  }

  componentWillUnmount() {
    this.stopListeningResults();
  }

  updateResults(results) {
    if(results !== null && results.hits !== null) {
      this.hits = results.hits.hits;
    }
  }

  labelAllAsRelevant() {
    this.labelAll(true);
  }

  labelAllAsIrrelevant() {
    this.labelAll(false);
  }

  labelAll(feedback) {
    let labels = {};
    this.hits.forEach(hit => labels[hit._source.url] = feedback);
    this.labelsManager.sendLabels(labels);
  }

  render() {
    return (
      <div className="label-all">
        <button className="btn btn-default" onClick={()=>this.labelAllAsRelevant()}><span className="glyphicon glyphicon-thumbs-up"></span>&nbsp;Mark all as Relevant</button>
        <button className="btn btn-default" onClick={()=>this.labelAllAsIrrelevant()}><span className="glyphicon glyphicon-thumbs-down"></span>&nbsp;Mark all as irrelevant</button>
      </div>
    )
  }

}

class Search extends React.Component {

  constructor(props) {
    super(props);
    this.crawlerId = this.props.match.params.crawler_id;
    const elasticsearchAddress = ACHE_API_ADDRESS + 'crawls/' + this.crawlerId;
    this.searchkit = new SearchkitManager(elasticsearchAddress, searchkitProps);
    this.labelsManager = new LabelsManager(this.crawlerId);
    this.hitItemElement = <HitItem labelsManager={this.labelsManager} />;
    this.state = {message:"Loading...", searchEnabled: false};
    api.get('/crawls/' + this.crawlerId + '/status')
       .then(this.setupSearch.bind(this));
  }

  setupSearch(status) {
    if(status === 'FETCH_ERROR') {
      this.setState({
        message: "Failed to connect to ACHE API to get crawler status.",
        searchEnabled: false
      });
      return;
    }
    if(!status.searchEnabled) {
      this.setState({
        message: "Search is not available for this crawl (it's only available when using ELASTICSEARCH data format).",
        searchEnabled: status.searchEnabled
      });
    } else {
      this.setState({
        message: "Done.",
        searchEnabled: status.searchEnabled
      });
    }
  }

  render() {
    const hitItemElement = this.hitItemElement;
    const enabled = this.state.searchEnabled;
    const message = this.state.message;
    let checkboxLabels = {
      "relevant":"Relevant",
      "irrelevant": "Irrelevant"
    }
    return (
      <div>
        { !enabled ?
          <div className="alert alert-danger message">
            <span className="glyphicon glyphicon-exclamation-sign" aria-hidden="true"></span> {message}
          </div>
          :
          <SearchkitProvider searchkit={this.searchkit} >
            <div className="row">

              <div className="col-sm-3">
                <RefinementListFilter id="filter_relevance" title="Relevance"
                  field="isRelevant" size={2} operator="OR"
                  translations={checkboxLabels} />
                <RefinementListFilter id="filter_domain" title="Domain"
                  field="domain" size={15} operator="OR" />
              </div>

              <div className="col-sm-9">
                  <SearchBox searchOnChange={true} searchThrottleTime={1000} />
                  <ActionBar>
                    <ActionBarRow>
              				<HitsStats translations={{"hitstats.results_found":"{hitCount} results found."}}/>
                      <ViewSwitcherToggle/>
                    </ActionBarRow>
                    <ActionBarRow>
                      <SelectedFilters/>
                      <ResetFilters/>
                    </ActionBarRow>
                  </ActionBar>
                  <Hits hitsPerPage={10} highlightFields={["title"]}
                    sourceFilter={["_id", "isRelevant", "title", "url", "retrieved", "text", "html"]}
                    itemComponent={hitItemElement} />
                  <LabelAllButtons searchkit={this.searchkit} />
                  <Pagination showNumbers={true}/>
              </div>
            </div>
          </SearchkitProvider>
        }
      </div>
    );
  }
}

export default Search;
