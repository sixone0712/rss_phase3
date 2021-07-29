import React from "react";
import ReactTransitionGroup from "react-addons-css-transition-group";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";

const ConfirmModal = React.memo(({isOpen, icon, message, leftBtn, rightBtn, style, actionBg, actionLeft, actionRight}) => {
    return (
        <>
            {isOpen ? (
                <ReactTransitionGroup
                    transitionName={"Custom-modal-anim"}
                    transitionEnterTimeout={200}
                    transitionLeaveTimeout={200}
                >
                    <div className="Custom-modal-overlay" onClick={actionBg} />
                    <div className="Custom-modal">
                        <div className="content-without-title">
                            <p><FontAwesomeIcon icon={icon} size="8x" /></p>
                            <p>{message}</p>
                        </div>
                        <div className="button-wrap">
                            <button className={"form-type left-btn " + style} onClick={actionLeft}>
                                {leftBtn}
                            </button>
                            <button
                                className={"form-type right-btn " + style}
                                onClick={actionRight}
                            >
                                {rightBtn}
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
        </>
    );
});

export default ConfirmModal;
