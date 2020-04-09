package ast;

public abstract class ParentNode extends BaseNode {
    public BaseNode child;

    public ParentNode(NodeType type, BaseNode child) {
        super(type);
        this.child = child;
    }

    @Override
    public String GetName() {
        return child.GetName();
    }
}