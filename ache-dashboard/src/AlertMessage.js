import React from 'react';

class Messages {

  data = [];

  error(text) {
    this.data.push({type:'error', message: text});
  }

  success(text) {
    this.data.push({type:'success', message: text});
  }

  display() {
    this.clearOldMessages();
    for(let m of this.data) {
      if(!m.firstShownAt)
        m.firstShownAt = Date.now();
    }
    return this.data;
  }

  clearOldMessages() {
    const timeLimitMs = 5000; // 5 secs
    let now = Date.now();
    this.data = this.data.filter(m =>
      !m.firstShownAt ||
      ((now - m.firstShownAt) < timeLimitMs)
    );
  }

}

class AlertMessage extends React.Component {

  render() {
    if(!this.props.message) return null;
    const text = this.props.message.message;
    let glyphName;
    let msgClass;
    if (this.props.message.type === 'error') {
      glyphName = 'glyphicon-exclamation-sign';
      msgClass = 'alert-danger';
    } else if (this.props.message.type === 'warn') {
      glyphName = 'glyphicon-exclamation-sign';
      msgClass = 'alert-warning';
    } else if (this.props.message.type === 'success') {
      glyphName = 'glyphicon-ok-circle';
      msgClass = 'alert-success';
    } else {
      glyphName = 'glyphicon-ok-circle';
      msgClass = 'alert-info';
    }
    return (
      <div className={'alert message ' + msgClass} >
        <span className={'glyphicon ' + glyphName } aria-hidden="true"></span>&nbsp;{text}
      </div>
    );
  }

}

class AlertMessages extends React.Component {

  render() {
    if(!this.props.messages || this.props.messages.length === 0)
      return null;
    else return (
      <div>
      { this.props.messages.map((msg, index) =>
         <AlertMessage key={index} message={msg} />
      )}
      </div>
    );
  }

}

export {Messages, AlertMessage, AlertMessages};
