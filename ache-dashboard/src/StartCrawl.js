import React from 'react';

import {ACHE_API_ADDRESS} from './Config';

class StartCrawl extends React.Component {

  constructor(props) {
    super(props);
    this.state = {
      crawlType: null,
      starting: false,
      modelFile: null
    };
    this.checkCrawlerStatus();
  }

  componentDidMount() {
    this.timerID = setInterval(
      () => this.checkCrawlerStatus(),
      2000 // update status every 1s
    );
  }

  componentWillUnmount() {
    clearInterval(this.timerID);
  }
  
  checkCrawlerStatus() {
    fetch(ACHE_API_ADDRESS + "/status")
      .then(function(response) {
        return response.json();
      }, function(error) {
        return 'FETCH_ERROR';
      })
      .then( (response) => {
        if(response === 'FETCH_ERROR') {
          this.state.serverError = true;
          this.state.serverMessage = "Failed to connect to ACHE API to get status.";
          this.forceUpdate();
        } else if (this.state.serverError === true) {
          this.state.serverError = false;
          this.state.serverMessage = null;
          this.forceUpdate();
        }
        if(response.crawlerRunning === true) {
          this.state.serverMessage = "Crawler is running. Open \"Monitoring\" tab for crawl details.";
          this.state.crawlerRunning = true;
          this.forceUpdate();
        } else {
          this.state.serverMessage = null;
          this.state.crawlerRunning = false;
          this.forceUpdate();
        }
      });
  }
  
  setCrawlType(crawlType) {
    this.state.crawlType = crawlType;
    this.forceUpdate();
  }
  
  cancelCrawl() {
    this.state.crawlType = null;
    this.state.seeds = null;
    this.state.seedsContent = null;
    this.state.modelFile = null;
    this.state.invalidModel = null;
    this.forceUpdate();
  }
  
  startCrawl(event) {
    console.log("Starting crawl...");
    this.state.starting = true;
    this.forceUpdate();
    var config = {
      method: 'POST',
      mode: 'cors',
      body: JSON.stringify({
        crawlType: this.state.crawlType,
        seeds: this.state.seeds,
        model: this.state.modelFile
      })
    };
    fetch(ACHE_API_ADDRESS + "/startCrawl", config)
      .then(function(response) {
        return response.json();
      }, function(error) {
        return 'FETCH_ERROR';
      })
      .then(this.updateResponse.bind(this));
  }
  
  updateResponse(response) {
    console.log(response);
    if('FETCH_ERROR' === response) {
      console.log("Failed to start crawler.");
      return
    }  
  }

  handleSelectModelFile(e) {
    const reader = new FileReader();
    const file = e.target.files[0];
    console.log(file.type);
    if('application/zip' === file.type) {
      reader.onload = (upload) => {
        this.state.modelFile = reader.result.split(',')[1];
        this.state.invalidModel = false;
        this.forceUpdate();
      };
      reader.readAsDataURL(file);
    } else {
      this.state.invalidModel = true;
      this.forceUpdate();
    }
  }

  handleSelectSeedFile(e) {
    const reader = new FileReader();
    const file = e.target.files[0];
    console.log(file.type);
    reader.onload = (upload) => {
      //TODO validate reader.result not empty and filetype
      let seedsContent = reader.result.replace(/\r\n/g, "\n")
      var seeds = seedsContent.split('\n')
      
      if(seeds !== null && seeds.length > 0) {
        let validUrls = []
        for(var i = 0; i < seeds.length; i++) {
          if(/^https?:\/\/.+/i.test(seeds[i])) {
            validUrls.push(seeds[i]);
          }
        }
        this.state.seeds = validUrls;
        this.state.seedsContent = validUrls.join('\n');
        this.forceUpdate();
      }
    };
    reader.readAsText(file);
  }
  
  render(){
    if(this.state.serverMessage !== null) {
      if(this.state.serverError === true) {
        return (
          <div className="alert alert-danger message">
            <span className="glyphicon glyphicon-exclamation-sign" aria-hidden="true"></span> {this.state.serverMessage}
          </div>
        );
      } else {
        return (
          <div className="alert alert-success message">
            <span className="glyphicon glyphicon-ok-circle" aria-hidden="true"></span>&nbsp;{this.state.serverMessage}
          </div>
        );
      }
    }
    const isStarting = this.state.starting;
    const hasValidSeeds = this.state.seeds && this.state.seeds.length;
    
    let enableStart = null;
    if (this.state.crawlType === 'DeepCrawl') {
      enableStart = hasValidSeeds > 0 && !isStarting;
    } else {
      enableStart = this.state.modelFile !== null && !isStarting;
    }
    
    if(this.state.crawlType === null) {
      return (
        <div className="row">
          <div className="col-md-6 col-md-offset-3">
            <h4 style={{'textAlign': 'center'}} >What type of crawl do would you like to start?</h4>
            <button type="button" className="btn btn-block btn-default btn-lg" onClick={()=>this.setCrawlType('DeepCrawl')}>Deep Website Crawl</button>
            <button type="button" className="btn btn-block btn-default btn-lg" onClick={()=>this.setCrawlType('FocusedCrawl')} >Focused Crawl</button>
          </div>    
        </div>
      );
    }
    
    return (
      <div className="row">
        <div className="col-md-10 col-md-offset-1">
          {(this.state.crawlType === 'DeepCrawl') && <h2>Start a <b>Deep Website  Crawl</b></h2> }
          {(this.state.crawlType === 'FocusedCrawl') && <h2>Start a <b>Focused Crawl</b></h2> }
          
          <form>
            { 
              (this.state.crawlType === 'DeepCrawl')
              &&
              <div>
                <p>All pages found within the websites contained in the seeds file will be crawled.</p>
                <div className="form-group">
                  <label htmlFor="seedsInputFile">Seeds file:</label>
                  <input type="file" className="form-control-file" id="seedsInputFile" aria-describedby="seedsFileHelp" onChange={(e)=>this.handleSelectSeedFile(e)} />
                  <small id="seedsFileHelp" className="form-text text-muted">Please select a text file containing a list of URLs to start the crawl. The file should contain one URL per line, and each URL should start with "http://" or "https://".</small>
                </div>
              </div>
            }
            { 
              (this.state.crawlType === 'FocusedCrawl')
              &&
              <div className="form-group">
                <label htmlFor="seedsInputFile">Model package:</label>
                <input type="file" className="form-control-file" id="modelInputFile" aria-describedby="modelFileHelp" onChange={(e)=>this.handleSelectModelFile(e)} />
                <small id="modelFileHelp" className="form-text text-muted">Please select the model file downloaded from DDT (&lt;domain-name&gt;_model.zip).</small>
              </div>
            }
            <div className="form-group">
              <div className="btn-toolbar">
                <button type="button" className="btn btn-default btn-md" onClick={()=>this.cancelCrawl()}><span className="glyphicon glyphicon-remove"></span>&nbsp;Cancel</button>
                <button disabled={!enableStart} type="button" className="btn btn-primary btn-md" onClick={(e)=>this.startCrawl(e)}><span className="glyphicon glyphicon-play"></span>&nbsp;Start Crawl</button>
              </div>
              { isStarting && <small className="form-text text-muted"><br/>Starting crawler... Hang tight.</small> }
            </div>
            { this.state.seeds && <p>Loaded {this.state.seeds.length} URLs from file.</p>}
            { this.state.seedsContent && <div><pre>{this.state.seedsContent}</pre></div> }
            { this.state.invalidModel && <p className="text-danger">Invalid model file. Select a zip file.</p>}
          </form>
        </div>
      </div>
    );
  
    
  }

}

export default StartCrawl;
