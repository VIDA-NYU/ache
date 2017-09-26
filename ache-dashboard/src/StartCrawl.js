import React from 'react';
import {api} from './RestApi';

class StartCrawl extends React.Component {

  constructor(props) {
    super(props);
    this.state = {};
    this.checkCrawlerStatus();
  }

  componentDidMount() {
    this.timerID = setInterval(
      () => this.checkCrawlerStatus(),
      2000 // update status every 2s
    );
  }

  componentWillUnmount() {
    clearInterval(this.timerID);
  }
  
  checkCrawlerStatus() {
    api.get("/status")
      .then( (response) => {
        if (response === 'FETCH_ERROR') {
          this.setState({
            serverError: true,
            serverMessage: "Failed to connect to ACHE API to get status."
          });
        } else {
          this.setState({serverError: false});
          if(response.crawlerRunning === true) {
            this.setState({
              crawlerRunning: true,
              serverMessage: "Crawler is running. Open \"Monitoring\" tab for crawl details."
            });
          } else {
            this.setState({crawlerRunning: false});
          }
        }
      });
  }
  
  setCrawlType(newCrawlType) {
    this.setState({crawlType: newCrawlType});
  }
  
  cancelCrawl() {
    this.setState({
      crawlType: null,
      seeds: null,
      seedsContent: null,
      modelFile: null,
      invalidModel: null,
      starting: false
    });
  }
  
  startCrawl(event) {
    this.setState({starting: true});
    var config = {
      method: 'POST',
      mode: 'cors',
      body: JSON.stringify({
        crawlType: this.state.crawlType,
        seeds: this.state.seeds,
        model: this.state.modelFile
      })
    };
    api.post("/startCrawl", config).then(this.updateResponse.bind(this));
  }
  
  updateResponse(response) {
    if('FETCH_ERROR' === response) {
      this.setState({
        serverError: true,
        serverMessage: "Failed to connect to server to start the crawler."
      });
    } else {
      if(response.crawlerStarted === false) {
        this.setState({
          starting: false,
          serverError: true,
          serverMessage: "Failed to start the crawler."
        });
      } else if(response.crawlerStarted === true) {
        this.setState({
          crawlerRunning: true,
          serverMessage: "Crawler started successfully."
        });
      }
    }
  }

  handleSelectModelFile(e) {
    const reader = new FileReader();
    const file = e.target.files[0];
    if('application/zip' === file.type) {
      reader.onload = (upload) => {
        this.setState({
          modelFile: reader.result.split(',')[1],
          invalidModel: false
        });
      };
      reader.readAsDataURL(file);
    } else {
      this.setState({
        invalidModel: true
      });
    }
  }

  handleSelectSeedFile(e) {
    const reader = new FileReader();
    const file = e.target.files[0];
    reader.onload = (upload) => {
      //TODO validate reader.result not empty and filetype
      let seedsContent = reader.result.replace(/\r\n/g, "\n");
      var seeds = seedsContent.split('\n');
      
      if(seeds !== null && seeds.length > 0) {
        let validUrls = []
        for(var i = 0; i < seeds.length; i++) {
          if(/^https?:\/\/.+/i.test(seeds[i])) {
            validUrls.push(seeds[i]);
          }
        }
        this.setState({
          seeds: validUrls,
          seedsContent: validUrls.join('\n')
        });
      }
    };
    reader.readAsText(file);
  }
  
  render(){
    // render error message
    if(this.state.serverError === true) {
      return (
        <div className="alert alert-danger message">
          <span className="glyphicon glyphicon-exclamation-sign" aria-hidden="true"></span>&nbsp;{this.state.serverMessage}
        </div>
      );
    }
    
    // render crawker running message
    if (this.state.crawlerRunning === true) {
      return (
        <div className="alert alert-success message">
          <span className="glyphicon glyphicon-ok-circle" aria-hidden="true"></span>&nbsp;{this.state.serverMessage}
        </div>
      );
    }
    
    // render crawl type selection buttons
    if(this.state.crawlType == null) {
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

    // render launch crawl pages
    const isStarting = this.state.starting;
    const hasValidSeeds = this.state.seeds && this.state.seeds.length;
    
    let enableStart = null;
    if (this.state.crawlType === 'DeepCrawl') {
      enableStart = hasValidSeeds > 0 && !isStarting;
    } else {
      enableStart = this.state.modelFile !== null && !isStarting;
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
