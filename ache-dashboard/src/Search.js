import React from 'react';
import {
  SearchkitManager, SearchkitProvider, SearchBox, Hits, RefinementListFilter,
  ActionBar, ActionBarRow, HitsStats, ViewSwitcherToggle, SelectedFilters,
  ResetFilters, RangeFilter, DynamicRangeFilter
} from "searchkit";


//const host = "http://demo.searchkit.co/api/movies"
const apiHost = "http://localhost:8080/";
const esHost = apiHost;
const searchkit = new SearchkitManager(esHost);

var formatDate = function(timestamp) {
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

class HitItem extends React.Component {

  extractDescription(input) {
    // try to extraction description from metatag og:description
    var ogdesc = input.html.match(/<meta property=\"og:description\" content=\"(.*?)\"/i);
    var clean = '';
    if(ogdesc !== null) {
      clean = ogdesc[1] + ' ' + input.text;;
    } else {
      clean = input.text;
    }
    clean = clean.replace(/\\n/g, " ");
    clean = clean.replace(/\s\s+/g, ' ' );
    let maxlimit = 350;
    return (clean.length > maxlimit) ? (clean.substring(0,maxlimit-3) + '...') : clean;
  }

  render() {
    const props = this.props;
    const desc = this.extractDescription(props.result._source)
    return (
      <div className={props.bemBlocks.item().mix(props.bemBlocks.container("item"))}>
        <div className={props.bemBlocks.item("details")}>
          <a href={props.result._source.url} target="_blank" className={props.bemBlocks.item("title")} dangerouslySetInnerHTML={{__html:props.result._source.title}}></a>
          <a href={props.result._source.url} target="_blank" className={props.bemBlocks.item("url")}   dangerouslySetInnerHTML={{__html:props.result._source.url}}></a>
          <div className={props.bemBlocks.item("description")} dangerouslySetInnerHTML={{__html:desc}}></div>
          <div className={props.bemBlocks.item("retrieved")} dangerouslySetInnerHTML={{__html:formatDate(props.result._source.retrieved)}}></div>
        </div>
      </div>
    )
  }
}

class Search extends React.Component {

  constructor(props) {
    super(props);
    var url = apiHost;
    fetch(url)
      .then(function(response) {
        return response.json();
      }, function(error) {
        return 'FETCH_ERROR';
      })
      .then(this.setupSearch.bind(this));
      this.state = {message:"Loading...", searchEnabled: false};
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

    const enabled = this.state.searchEnabled;
    const message = this.state.message;

    return (
      <div>


        { !enabled ?
          <div className="alert alert-danger message">
            <span className="glyphicon glyphicon-exclamation-sign" aria-hidden="true"></span> {message}
          </div>
          :
          <SearchkitProvider searchkit={searchkit} >
            <div className="row">

              <div className="col-md-3">
                <RefinementListFilter id="filter_relevance" title="Relevance" field="_type" size={2} operator="OR" />
                <RefinementListFilter id="filter_domain" title="Domain" field="domain" size={10} operator="OR" />
                {/*
                <RefinementListFilter id="filter_words" title="Words" field="words" size={5}/>
                <RangeFilter min={0} max={100} field="timestamp_crawl" id="timestamp_crawl" title="Crawl Time" showHistogram={true}/>
                <DynamicRangeFilter field="timestamp_index" id="timestamp_index" title="Indexing Time" rangeFormatter={formatDate}/>
                */}
              </div>

              <div className="col-md-9">

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

                  <Hits hitsPerPage={10} highlightFields={["title"]} sourceFilter={["_id", "title", "url", "retrieved", "text", "html"]} itemComponent={HitItem} />

              </div>
            </div>
          </SearchkitProvider>
        }
      </div>
    );
  }
}

export default Search;
