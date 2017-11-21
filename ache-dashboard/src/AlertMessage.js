import React from 'react';

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
      msgClass = 'alert-primary';
    }
    return (
      <div className="row">
        <div className={'alert message ' + msgClass} >
          <span className={'glyphicon ' + glyphName } aria-hidden="true"></span>&nbsp;{text}
        </div>
      </div>
    );
  }
}

export default AlertMessage;
