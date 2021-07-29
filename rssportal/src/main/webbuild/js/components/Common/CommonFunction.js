import React, {useCallback} from "react";
import PaginationComponent from "react-reactstrap-pagination";
import * as Define from "../../define";

const MAX_STRING_BYTES = 200;

export const filePaginate = (items, pageNumber, pageSize) => {
    const startIndex = (pageNumber - 1) * pageSize;
    return items.slice( startIndex, startIndex +  pageSize);
}

export const RenderPagination = ({pageSize, itemsCount, onPageChange, currentPage, className}) => {
    const pageCount = Math.ceil(itemsCount / pageSize);

    if (pageCount === 1) {
        return null;
    }

    return (
        <div className={className}>
            <PaginationComponent
                totalItems={itemsCount}
                pageSize={pageSize}
                onSelect={onPageChange}
                defaultActivePage={currentPage}
                maxPaginationNumbers={10}
                firstPageText={"«"}
                previousPageText={"‹"}
                nextPageText={"›"}
                lastPageText={"»"}
            />
        </div>
    );
}

export const propsCompare = (prevProps, nextProps) => {
    return JSON.stringify(prevProps) === JSON.stringify(nextProps);
};

export const stringBytes = (s) => {
    let b, i, c = 0;
    for(b=i=0; c=s.charCodeAt(i++); b += c >> 11 ? 3 : c >> 7 ? 2 : 1);
    return b;
};

export const invalidCheckVFTP = (type, setMsg, setModal, currentContext, currentDataType) => {
    const currentCommand = type === Define.PLAN_TYPE_VFTP_COMPAT ? currentContext : currentDataType + currentContext;
    if (stringBytes(currentCommand) > MAX_STRING_BYTES) {
        setMsg("This command is too long.");
        setModal();
        return true;
    } else {
        if (type === Define.PLAN_TYPE_VFTP_SSS) {
            if (currentDataType === "") {
                setMsg("Data type is empty.");
                setModal();
                return true;
            }
        } else {
            if (currentContext === "") {
                setMsg("Context is empty.");
                setModal();
                return true;
            }
        }
    }
    return false;
};

export const setCurrentCommand = (type, currentDataType, currentContext) => {
    if (type === Define.PLAN_TYPE_VFTP_COMPAT) {
        return "%s-%s-" + currentContext;
    } else {
        if (currentContext.length > 0) {
            return currentDataType + "-%s-%s-" + currentContext;
        } else {
            return currentDataType + "-%s-%s";
        }
    }
};