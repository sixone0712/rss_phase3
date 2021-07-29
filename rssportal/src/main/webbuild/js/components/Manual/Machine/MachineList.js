import React, {Component} from "react";
import {Button, ButtonToggle, Card, CardBody, Col, FormGroup} from "reactstrap";
import {connect} from "react-redux";
import {bindActionCreators} from "redux";
import * as viewListActions from "../../../modules/viewList";
import * as API from '../../../api'
import EquipmentCollapse from "./EquipmentCollapse";
import {faSyncAlt} from "@fortawesome/free-solid-svg-icons";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import * as Define from "../../../define";

class MachineList extends Component {
  constructor(props) {
    super(props);
  }

  checkMachineItem = (e) => {
    const idx = e.target.id.split('_{#div#}_')[1];
    API.checkToolInfoList(this.props, idx);
  };

  checkAllMachineItem = (checked) => {
    API.checkAllToolInfoList(this.props, checked, true);
  };

  render() {
    const titleList = API.getEquipmentList(this.props);
    const machineList = API.getToolInfoList(this.props);
    const { toolInfoDisplay } = this.props;
    const runningList = machineList.filter((item) => {
      if(toolInfoDisplay === "vftp") return item.ots && item.vftpConnected;
      else return item.ots && item.ftpConnected;
    });
    const ItemsChecked = runningList.length === this.props.toolInfoListCheckCnt;

    return (
      <Card className="ribbon-wrapper machinelist-card">
        <CardBody className="custom-scrollbar manual-card-body">
          <div className="ribbon ribbon-clip ribbon-primary">Machine</div>
          <Col>
            <FormGroup className="machinelist-form-group">
              {titleList.map((title, index) => {
                return (
                  <EquipmentCollapse
                    key={index}
                    structId={title.fabName}
                    machineList={machineList}
                    checkMachineItem={this.checkMachineItem}
                    toolInfoDisplay={toolInfoDisplay}
                  />
                );
              })}
            </FormGroup>
          </Col>
          <div className="card-btn-area">
            <Button
                size="sm"
                className="manual-refresh-btn"
                onClick={() => this.props.viewListActions.viewUpdateManualToolInfoList(Define.REST_SYSTEM_GET_MACHINES)}
            >
              <FontAwesomeIcon icon={faSyncAlt}/>
            </Button>
            <ButtonToggle
              outline
              size="sm"
              color="info"
              className={"machinelist-btn" + (ItemsChecked ? " active" : "")}
              onClick={()=> this.checkAllMachineItem(!ItemsChecked)}
            >
              All
            </ButtonToggle>
          </div>
        </CardBody>
      </Card>
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
)(MachineList);
