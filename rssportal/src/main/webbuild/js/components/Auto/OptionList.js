import React, {Component} from "react";
import {connect} from "react-redux";
import {bindActionCreators} from "redux";
import * as autoPlanActions from "../../modules/autoPlan";
import {Col, CustomInput, FormGroup, Input, Label, PopoverBody, PopoverHeader, UncontrolledPopover} from "reactstrap";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faExclamation} from "@fortawesome/free-solid-svg-icons";
import {DatetimePicker} from "rc-datetime-picker";
import ReactTransitionGroup from "react-addons-css-transition-group";
import {Select} from "antd";
import * as Define from "../../define";
import moment from "moment";

const { Option } = Select;
const intervalType = {
  INTERVAL_MINUTE: "minute",
  INTERVAL_HOUR: "hour",
  INTERVAL_DAY: "day"
};

class RSSautoformlist extends Component {
  constructor(props) {
    super(props);
    this.state = {
      currentModal: 0,
      modalOpen: false,
      endPeriod: !!props.autoPlan.get("to")
    };

    this.periodFrom = React.createRef();
    this.periodTo = React.createRef();
    this.collectStartField = React.createRef();
  }

  handleDateChange = (idx, moment) => {
    const { autoPlanActions } = this.props;

    switch (idx) {
      case Define.AUTO_DATE_PERIOD_FROM:
        autoPlanActions.autoPlanSetFrom(moment);
        break;

      case Define.AUTO_DATE_PERIOD_TO:
        autoPlanActions.autoPlanSetTo(moment);
        break;

      case Define.AUTO_DATE_COLLECT_START:
        autoPlanActions.autoPlanSetCollectStart(moment);
        break;

      default:
        break;
    }
  };

  getDateValue = idx => {
    const { autoPlan } = this.props;
    switch (idx) {
      case Define.AUTO_DATE_PERIOD_FROM:
        return autoPlan.get("from");

      case Define.AUTO_DATE_PERIOD_TO:
        return autoPlan.get("to");

      case Define.AUTO_DATE_COLLECT_START:
        return autoPlan.get("collectStart");

      default:
        return;
    }
  };

  openModal = idx => {
    switch(idx) {
      case Define.AUTO_DATE_PERIOD_FROM:
        this.periodFrom.current.blur();
        break;

      case Define.AUTO_DATE_PERIOD_TO:
        this.periodTo.current.blur();
        break;

      case Define.AUTO_DATE_COLLECT_START:
        this.collectStartField.current.blur();
        break;

      default:
        break;
    }

    this.setState({
      currentModal: idx,
      modalOpen: true
    });
  };

  closeModal = () => {
    this.setState({
      modalOpen: false
    });
  };

  handleModeChange = mode => {
    const { autoPlanActions } = this.props;
    autoPlanActions.autoPlanSetCollectType(mode);
  };

  handlePlanIdChange = e => {
    const { autoPlanActions } = this.props;
    autoPlanActions.autoPlanSetPlanId(e.target.value);
  }

  handleIntervalChange = e => {
    const { autoPlanActions } = this.props;
    autoPlanActions.autoPlanSetInterval(e.target.value);
  }

  handleIntervalUnitChange = value => {
    const { autoPlanActions } = this.props;
    autoPlanActions.autoPlanSetIntervalUnit(value);
  }

  handleDiscriptionChange = e => {
    const { autoPlanActions } = this.props;
    autoPlanActions.autoPlanSetDescription(e.target.value);
  }

  componentDidMount() {
    const { isNew, autoPlanActions } = this.props;
    if (isNew) {
      autoPlanActions.autoPlanSetCollectStart(moment());
    }
    const { to } = this.props.autoPlan.toJS();
    if(!to) this.setState({ endPeriod: false })
  }

  onChangeEndPeriod = (value) => {
    const { autoPlanActions } = this.props;
    if(value) {
      autoPlanActions.autoPlanSetTo(moment().endOf('day'));
      this.setState({ endPeriod: true });
    } else {
      autoPlanActions.autoPlanSetTo("");
      this.setState({ endPeriod: false });
    }
  }

  onChangeSplitCompression = (value) => {
    const { autoPlanActions } = this.props;
    autoPlanActions.autoPlanSetSeparatedZip(value);
  }

  render() {
    const { currentModal, modalOpen, endPeriod } = this.state;
    const { autoPlan } = this.props;
    const { planId, collectType, interval, intervalUnit, from, to, collectStart, description, separatedZip } = autoPlan.toJS();

    return (
        <div className="form-section optionlist">
          <Col className="pd-0">
            <div className="form-header-section">
              <div className="form-title-section">
                Detail Options
                <p>Set detail plan options.</p>
              </div>
            </div>
            <div className="dis-flex align-center">
              <FormGroup className="auto-plan-optionlist-form-group">
                <FormGroup>
                  <Label for="plan_id" className="input-label">
                    Plan Name
                  </Label>
                  <Input
                      type="text"
                      id="plan_id"
                      bsSize="sm"
                      className="half-width"
                      value={planId}
                      maxLength="32"
                      onChange={this.handlePlanIdChange}
                  />
                  <UncontrolledPopover
                      placement="top-end"
                      target="plan_id"
                      className="auto-plan"
                      trigger="hover"
                      delay={{ show: 300, hide: 0 }}
                  >
                    <PopoverHeader>Plan ID</PopoverHeader>
                    <PopoverBody>
                      <p>
                        <FontAwesomeIcon icon={faExclamation} />{" "}
                        There are no restrictions on the types of characters that can be entered.
                      </p>
                      <p>
                        <FontAwesomeIcon icon={faExclamation} />{" "}
                        Special characters cannot be entered at the beginning or end.
                      </p>
                      <p>
                        <FontAwesomeIcon icon={faExclamation} />{" "}
                        Allowed to be at least 3 characters long and up to 32 characters long.
                      </p>
                    </PopoverBody>
                  </UncontrolledPopover>
                </FormGroup>
                <FormGroup>
                  <Label for="plan_period" className="input-label">
                    Period
                  </Label>
                  <FormGroup className="period-section">
                    <input
                        type="text"
                        readOnly
                        className="form-control-sm form-control"
                        value={from.format("YYYY-MM-DD HH:mm")}
                        onClick={() => this.openModal(Define.AUTO_DATE_PERIOD_FROM)}
                        ref={this.periodFrom}
                    />
                    <span className="split-character">~</span>
                    <input
                        type="text"
                        readOnly
                        className={endPeriod ? "form-control-sm form-control" : "form-control-sm form-control disable"}
                        value={to ? to.format("YYYY-MM-DD HH:mm") : ""}
                        onClick={() => this.openModal(Define.AUTO_DATE_PERIOD_TO)}
                        ref={this.periodTo}
                    />
                    <span className="split-character"></span>
                    <CustomInput
                        id="end_period"
                        className="end_period"
                        type="checkbox"
                        label="End Period"
                        value={endPeriod}
                        checked={endPeriod}
                        onChange={() => this.onChangeEndPeriod(!endPeriod)}
                     />
                  </FormGroup>
                </FormGroup>
                <FormGroup className="start-section">
                  <Label for="plan_start" className="input-label">
                    Start
                  </Label>
                  <input
                      type="text"
                      readOnly
                      // className="form-control-sm form-control half-width"
                      className="form-control-sm form-control"
                      value={collectStart.format("YYYY-MM-DD HH:mm")}
                      onClick={() => this.openModal(Define.AUTO_DATE_COLLECT_START)}
                      ref={this.collectStartField}
                  />
                </FormGroup>
                <FormGroup>
                  <Label for="plan_mode" className="input-label">
                    Mode
                  </Label>
                  <FormGroup className="dis-flex mode-section">
                    <CustomInput
                        type="radio"
                        id="mode_continue"
                        name="collection_mode"
                        label="Continuous"
                        checked={collectType === Define.AUTO_MODE_CONTINUOUS}
                        onChange={() => this.handleModeChange(Define.AUTO_MODE_CONTINUOUS)}
                    />
                    <CustomInput
                        type="radio"
                        id="mode_cycle"
                        name="collection_mode"
                        label="Cycle"
                        className="mode-cycle"
                        checked={collectType === Define.AUTO_MODE_CYCLE}
                        onChange={() => this.handleModeChange(Define.AUTO_MODE_CYCLE)}
                    />
                    <div
                        className={
                          "sub-option " +
                          (collectType === Define.AUTO_MODE_CONTINUOUS ? "hidden" : "show")
                        }
                    >
                      <Input
                          type="text"
                          bsSize="sm"
                          id="plan_cycle_interval"
                          value={interval}
                          maxLength={setIntervalMaxLength(intervalUnit)}
                          onChange={this.handleIntervalChange}
                      />
                      <UncontrolledPopover
                          placement="top"
                          target="plan_cycle_interval"
                          className="auto-plan"
                          trigger="hover"
                          delay={{ show: 300, hide: 0 }}
                      >
                        <PopoverHeader>Cycle Interval</PopoverHeader>
                        <PopoverBody>
                          <p>
                            <FontAwesomeIcon icon={faExclamation} />{" "}
                            Minute: You can enter from 1 to 59.
                          </p>
                          <p>
                            <FontAwesomeIcon icon={faExclamation} />{" "}
                            Hour: You can enter from 1 to 23.
                          </p>
                          <p>
                            <FontAwesomeIcon icon={faExclamation} />{" "}
                            Day: You can enter from 1 to 365.
                          </p>
                        </PopoverBody>
                      </UncontrolledPopover>
                      <Select
                          defaultValue= {intervalUnit}
                          onChange={this.handleIntervalUnitChange}
                      >
                        <Option value={Define.AUTO_UNIT_MINUTE}>Minute</Option>
                        <Option value={Define.AUTO_UNIT_HOUR}>Hour</Option>
                        <Option value={Define.AUTO_UNIT_DAY}>Day</Option>
                      </Select>
                    </div>
                  </FormGroup>
                </FormGroup>
                <FormGroup>
                  <Label for="plan_desc" className="input-label">
                    Description
                  </Label>
                  <Input
                      type="text"
                      id="plan_desc"
                      bsSize="sm"
                      className="half-width"
                      maxLength="40"
                      value={description}
                      onChange={this.handleDiscriptionChange}
                  />
                  <UncontrolledPopover
                      placement="top-end"
                      target="plan_desc"
                      className="auto-plan"
                      trigger="hover"
                      delay={{ show: 300, hide: 0 }}
                  >
                    <PopoverHeader>Description</PopoverHeader>
                    <PopoverBody>
                      <p>
                        <FontAwesomeIcon icon={faExclamation} />{" "}
                        You can register a collection plan without entering the description.
                      </p>
                      <p>
                        <FontAwesomeIcon icon={faExclamation} />{" "}
                        There are no restrictions on the types of characters that can be entered.
                      </p>
                      <p>
                        <FontAwesomeIcon icon={faExclamation} />{" "}
                        Special characters cannot be entered at the beginning or end.
                      </p>
                      <p>
                        <FontAwesomeIcon icon={faExclamation} />{" "}
                        Allowed to be at least 3 characters long and up to 40 characters long.
                      </p>
                    </PopoverBody>
                  </UncontrolledPopover>
                </FormGroup>
                <FormGroup>
                  <Label for="plan_split_compression" className="input-label">
                    Split compression
                  </Label>
                  <FormGroup className="dis-flex  split-compression-section">
                    <CustomInput
                        type="radio"
                        id="split_compression_yes"
                        name="split_compression"
                        label="Yes"
                        checked={separatedZip}
                        onChange={() => this.onChangeSplitCompression(true)}
                    />
                    <CustomInput
                        type="radio"
                        id="split_compression_no"
                        name="split_compression"
                        label="No"
                        className="split_compression_no"
                        checked={!separatedZip}
                        onChange={() => this.onChangeSplitCompression(false)}
                    />
                  </FormGroup>
                </FormGroup>
              </FormGroup>
            </div>
          </Col>
          {modalOpen ? (
              <ReactTransitionGroup
                  transitionName={"Custom-modal-anim"}
                  transitionEnterTimeout={200}
                  transitionLeaveTimeout={200}
              >
                <div className="Custom-modal-overlay" onClick={this.closeModal} />
                <div className="Custom-modal auto-plan-calendar-modal">
                  <p className="title">Date Setting</p>
                  <div className="content-with-title">
                    <FormGroup>
                      <CreateDatetimePicker
                          moment={this.getDateValue(currentModal)}
                          idx={currentModal}
                          changer={this.handleDateChange}
                      />
                    </FormGroup>
                  </div>
                  <div className="button-wrap">
                    <button
                        className="auto-plan alert-type"
                        onClick={this.closeModal}
                    >
                      Close
                    </button>
                  </div>
                </div>
              </ReactTransitionGroup>
          ) : (
              <ReactTransitionGroup
                  transitionName={"Custom-modal-anim"}
                  transitionEnterTimeout={200}
                  transitionLeaveTimeout={200}
              />
          )}
        </div>
    );
  }
}

export class CreateDatetimePicker extends Component {

  handleChange = moment => {
    const { idx, changer } = this.props;
    changer(idx, moment);
  };

  render() {
    const { moment } = this.props;

    return <DatetimePicker moment={moment} onChange={this.handleChange} />;
  }
}

const setIntervalMaxLength = (type) => {
  let intervalMax = 2;

  switch(type) {
    case intervalType.INTERVAL_DAY:
      intervalMax = 3;
      break;

    case intervalType.INTERVAL_HOUR:
    case intervalType.INTERVAL_MINUTE:
    default:
      break;
  };

  return intervalMax;
}

export default connect(
    (state) => ({
      autoPlan: state.autoPlan.get('autoPlan'),
    }),
    (dispatch) => ({
      autoPlanActions: bindActionCreators(autoPlanActions, dispatch),
    })
)(RSSautoformlist);