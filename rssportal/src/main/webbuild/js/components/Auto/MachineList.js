import React, {Component} from "react";
import {connect} from "react-redux";
import {bindActionCreators} from "redux";
import * as viewListActions from "../../modules/viewList";
import * as API from '../../api'

import {Button, ButtonToggle, Col, FormGroup} from "reactstrap";
import {Collapse} from "react-collapse";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faBars, faSyncAlt} from "@fortawesome/free-solid-svg-icons";
import CheckBox from "../Common/CheckBox";
import * as Define from "../../define";

class RSSautomachinelist extends Component {
  constructor(props) {
    super(props);
  }

  checkAutoMachineItem = (e) => {
    const idx = e.target.id.split('_{#div#}_')[1];
    API.checkToolInfoList(this.props, idx);
  };

  checkAutoAllMachineItem = (checked) => {
    API.checkAllToolInfoList(this.props, checked, false);
  };

  render() {
    const titleList = API.getEquipmentList(this.props);
    const machineList = API.getToolInfoList(this.props);
    const checkedlist = machineList.filter((item) => item.ots);
    const ItemsChecked = checkedlist.length === this.props.toolInfoListCheckCnt;
    const { toolInfoDisplay } = this.props;

    return (
        <div className="form-section machinelist">
          <Col className="pd-0">
            <div className="form-header-section">
              <div className="form-title-section">
                Machine List
                <p>Select a machine from the list.</p>
              </div>
              <div className="form-btn-section">
                <Button
                    size="sm"
                    className="auto-refresh-btn"
                    onClick={() => this.props.viewListActions.viewUpdateAutoToolInfoList(Define.REST_SYSTEM_GET_MACHINES)}
                >
                  <FontAwesomeIcon icon={faSyncAlt}/>
                </Button>
                <ButtonToggle
                    outline
                    size="sm"
                    color="info"
                    className={"form-btn" + (ItemsChecked ? " active" : "")}
                    onClick={()=> this.checkAutoAllMachineItem(!ItemsChecked)}
                >
                  All
                </ButtonToggle>
              </div>
            </div>
            <FormGroup className="custom-scrollbar auto-plan-form-group machinelist">
              {titleList.map((title, index) => {
                console.log(title, index);
                return (
                    <div className="machine-section" key={index}>
                      <MachineCollapse
                          structId={title.fabName}
                          machineList={machineList}
                          checkItem={this.checkAutoMachineItem}
                          toolInfoDisplay={toolInfoDisplay}
                      />
                    </div>
                );
              })}
            </FormGroup>
          </Col>
        </div>
    );
  }
}

export class MachineCollapse extends Component {
  constructor(props) {
    super(props);
    this.state = {
      isOpened: true
    };
  }

  toggle = () => {
    this.setState({
      isOpened: !this.state.isOpened
    });
  };

  render() {
    const { isOpened } = this.state;
    const { machineList, structId, checkItem, toolInfoDisplay } = this.props;

    return (
        <>
          <div className="collapse-title" onClick={this.toggle}>
            <FontAwesomeIcon icon={faBars} /> {structId}
          </div>
          <Collapse isOpened={isOpened}>
            {machineList.map((machine, key) => {
              if (machine.structId === structId) {
                return (
                    <div className="custom-control custom-checkbox" key={key}>
                      <CheckBox
                          auto={true}
                          status={toolInfoDisplay === "vftp" ? machine.vftpConnected : machine.ftpConnected}
                          index={machine.keyIndex}
                          name={machine.targetname}
                          isChecked={machine.checked}
                          labelClass={"form-check-label"}
                          handleCheckboxClick={checkItem}
                      />
                    </div>
                );
              } else {
                return "";
              }
            })}
          </Collapse>
        </>
    );
  }
}

export default connect(
    (state) => ({
      equipmentList: state.viewList.get('equipmentList'),
      toolInfoList: state.viewList.get('toolInfoList'),
      toolInfoListCheckCnt: state.viewList.get('toolInfoListCheckCnt'),
      toolInfoDisplay: state.viewList.get('toolInfoDisplay'),
    }),
    (dispatch) => ({
      viewListActions: bindActionCreators(viewListActions, dispatch),
    })
)(RSSautomachinelist);