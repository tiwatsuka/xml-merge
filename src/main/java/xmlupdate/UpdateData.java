package xmlupdate;

import java.util.List;

public class UpdateData{

    private List<PositionData> createList;

    private List<String> updateList;

    private List<String> deleteList;

    public void setCreateList(List<PositionData> createList){
        this.createList = createList;
    }

    public void setUpdateList(List<String> updateList){
        this.updateList = updateList;
    }

    public void setDeleteList(List<String> deleteList){
        this.deleteList = deleteList;
    }

    public List<PositionData> getCreateList(){
        return createList;
    }

    public List<String> getUpdateList(){
        return updateList;
    }

    public List<String> getDeleteList(){
        return deleteList;
    }

    public static class PositionData{

        private String source;

        private String target;

        private boolean insertBefore;

        public void setSource(String source){
            this.source = source;
        }

        public void setTarget(String target){
            this.target = target;
        }

        public void setInsertBefore(boolean insertBefore){
            this.insertBefore = insertBefore;
        }

        public String getSource(){
            return source;
        }
        
        public String getTarget(){
            return target;
        }

        public boolean isInsertBefore(){
            return insertBefore;
        }
    }
}
