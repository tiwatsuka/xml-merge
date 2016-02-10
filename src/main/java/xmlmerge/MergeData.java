package xmlmerge;

import java.util.List;

public class MergeData{

    private List<CreateData> createList;

    private List<UpdateData> updateList;

    private List<String> deleteList;

    public void setCreateList(List<CreateData> createList){
        this.createList = createList;
    }

    public void setUpdateList(List<UpdateData> updateList){
        this.updateList = updateList;
    }

    public void setDeleteList(List<String> deleteList){
        this.deleteList = deleteList;
    }

    public List<CreateData> getCreateList(){
        return createList;
    }

    public List<UpdateData> getUpdateList(){
        return updateList;
    }

    public List<String> getDeleteList(){
        return deleteList;
    }

    public static class CreateData{

        private String source;

        private String target;

        private boolean insertBefore = false;
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

    public static class UpdateData{

        private String path;

        private boolean recursive = false;

        public void setPath(String path){
            this.path = path;
        }

        public void setRecursive(boolean recursive){
            this.recursive = recursive;
        }

        public String getPath(){
            return path;
        }

        public boolean isRecursive(){
            return recursive;
        }

    }
}
