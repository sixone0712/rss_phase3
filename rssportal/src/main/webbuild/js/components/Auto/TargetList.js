import React, {Component} from "react";
import {connect} from "react-redux";
import {bindActionCreators} from "redux";
import * as viewListActions from "../../modules/viewList";
import * as API from '../../api'
import {ButtonToggle, Col, FormGroup, Input} from "reactstrap";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faExclamationCircle, faSearch} from "@fortawesome/free-solid-svg-icons";
import CheckBox from "../Common/CheckBox";

const SECTION_DISPLAY_ITEM = 10;

class RSSautoTargetlist extends Component {
  constructor(props) {
    super(props);
    /*    const logInfoList = API.getLogInfoList(props);*/
    const original = API.getLogInfoList(props);
    const logInfoList = original.filter(item => item.auto && item.display);
    logInfoList.map(info=> {info.logName = info.logCode+"_"+info.logName});
    const sectionList = this.createTargetSection(logInfoList);
    let sectionIdx = 0;
    let writeCount = 1;
    const targetList = logInfoList.map(item => {
      let title = sectionList[sectionIdx].title;
      writeCount++;
      if (writeCount > 10) {
        sectionIdx++;
        writeCount = 1;
      }
      return {
        title: title,
        ...item,
      }
    })

    this.state = {
      sectionList: sectionList,
      filteredData: targetList,
      query: "",
      showSearch: false
    };
 }

  handleSearchToggle = () => {
    const { showSearch } = this.state;

    this.setState({
      ...this.state,
      showSearch: !showSearch,
      query: ""
    }, () => {
      if (showSearch === true) {
        const original = API.getLogInfoList(this.props);
        const logInfoList = original.filter(item => item.auto && item.display);
        logInfoList.map(info=> {info.logName = info.logCode+"_"+info.logName});
        this.createFilteredData(logInfoList);
        //this.createFilteredData(API.getLogInfoList(this.props));
      }
    });
  };

  selectAllItem = async (check) => {
    const { filteredData } = this.state;

    if (filteredData.length > 0) {
      const actionList = filteredData.map(item => item.keyIndex);
      const newFilterData = filteredData.map(item => {
        item.checked = check;
        return item;
      });

      await this.setState({
        filteredData: newFilterData
      }, () => {
        API.checkAllLogInfoList(this.props, check, actionList);
      })
    }
  };

  handleCheckboxClick = async e => {
    const idx = e.target.id.split('_{#div#}_')[1];

    const newFilterData = this.state.filteredData.map(item => {
      if(item.keyIndex === parseInt(idx)) {
        item.checked = !item.checked;
      }
      return item;
    });

    await this.setState({
      ...this.state,
      filteredData: newFilterData,
    }, () => {
      API.checkLogInfoList(this.props, idx);
    })
  }

  handleSearch = e => {
    const original = API.getLogInfoList(this.props);
    const targetList = original.filter(item => item.auto && item.display);
    targetList.map(info=> {info.logName = info.logCode+"_"+info.logName});

    // const targetList = API.getLogInfoList(this.props);
    const query = e.target.value;
    const filteredData = targetList.filter(element => {
      return element.logName.toLowerCase().includes(query.toLowerCase());
    });

    this.setState({
      ...this.state,
      query: query
    }, () => {
      this.createFilteredData(filteredData);
    });
  };

  createFilteredData = list => {
    const sectionList = this.createTargetSection(list);
    let sectionIdx = 0;
    let writeCount = 1;

    const targetList = list.map(item => {
      let title = sectionList[sectionIdx].title;
      writeCount++;
      if (writeCount > 10) {
        sectionIdx++;
        writeCount = 1;
      }
      return {
        title: title,
        ...item,
      }
    })

    this.setState({
      sectionList: sectionList,
      filteredData: targetList
    });
  };

  createTargetSection = list => {
    const count =
        list.length < SECTION_DISPLAY_ITEM
            ? 1
            : Math.ceil(list.length / SECTION_DISPLAY_ITEM);
    const targetSection = [];

    for (let idx = 1; idx <= count; idx++) {
      const tempData = { title: "section" + idx };
      targetSection.push(tempData);
    }

    return targetSection;
  };

  render() {
    const {
      showSearch,
      sectionList,
      filteredData,
      query
    } = this.state;

    let checkedCnt = 0;
    filteredData.map(ele => { if (ele.checked) { checkedCnt++; }});
    const ItemsChecked = checkedCnt === 0 ? false : checkedCnt === filteredData.length;

    return (
        <div className="form-section targetlist">
          <Col className="pdl-10 pdr-0">
            <div className="form-header-section">
              <div className="form-title-section">
                Target List
                <p>Select a target from the list.</p>
              </div>
              <div className="form-btn-section dis-flex">
                <div
                    className={"search-btn-area" + (showSearch ? " active" : "")}
                >
                  <ButtonToggle
                      outline
                      size="sm"
                      color="info"
                      className={"form-btn" + (showSearch ? " active" : "")}
                      onClick={this.handleSearchToggle}
                  >
                    <FontAwesomeIcon icon={faSearch} />
                  </ButtonToggle>
                  <FormGroup>
                    <Input
                        type="text"
                        className="form-search-input"
                        placeholder="Enter the Target name to search."
                        value={query}
                        onChange={this.handleSearch}
                    />
                  </FormGroup>
                </div>
                <ButtonToggle
                    outline
                    size="sm"
                    color="info"
                    className={"form-btn toggle-all" + (ItemsChecked ? " active" : "")}
                    onClick={() => this.selectAllItem(!ItemsChecked)}
                >
                  All
                </ButtonToggle>
              </div>
            </div>
            <FormGroup className="custom-scrollbar auto-plan-form-group targetlist pd-5">
              {filteredData.length > 0 ? (
                  sectionList.map(section => {
                    return (
                        <div key={section.title} className="checkbox-section">
                          <CreateCheckBox
                              title={section.title}
                              list={filteredData}
                              handleCheckboxClick={this.handleCheckboxClick}
                          />
                        </div>
                    );
                  })
              ) : (
                  <div className="search-error-area">
                    <p>
                      <FontAwesomeIcon icon={faExclamationCircle} size="8x" />
                    </p>
                    <p>Target not found.</p>
                  </div>
              )}
            </FormGroup>
          </Col>
        </div>
    );
  }
}

export const CreateCheckBox = props => {
  const { title, list, handleCheckboxClick } = props;

  return (
      <>
        {list.map((item, key) => {
          if (item.title === title) {
            return (
                <div className="custom-control custom-checkbox" key={key}>
                  <CheckBox
                      index={item.keyIndex}
                      name={item.logName}
                      isChecked={item.checked}
                      labelClass={"form-check-label"}
                      handleCheckboxClick={handleCheckboxClick}
                  />
                </div>
            );
          } else {
            return "";
          }
        })}
      </>
  );
};

export default connect(
    (state) => ({
      logInfoList: state.viewList.get('logInfoList'),
    }),
    (dispatch) => ({
      viewListActions: bindActionCreators(viewListActions, dispatch),
    })
)(RSSautoTargetlist);