package com.iiitd.erp.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.Timer;

/**
 * ViewGradesWindow
 * - Shows a student's grades in a table.
 */
public class ViewGradesWindow extends JFrame {

    public static void main(String[] args) { SwingUtilities.invokeLater(() -> new ViewGradesWindow().setVisible(true)); }

    public ViewGradesWindow() {
        setTitle("Grades");
        setSize(1000, 640);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        add(new HeaderBar(), BorderLayout.NORTH);
        add(new FooterBar(), BorderLayout.SOUTH);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(new Color(245,248,251));
        add(main, BorderLayout.CENTER);

        main.add(new SlidingPanel(new LeftRail()), BorderLayout.WEST);
        main.add(content(), BorderLayout.CENTER);

        setVisible(true);
    }

    private JComponent content(){
        JPanel p = new JPanel(new BorderLayout()); p.setOpaque(false);
        JLabel title = new JLabel("GRADE REPORT"); title.setFont(new Font("Segoe UI", Font.BOLD, 34)); title.setBorder(new EmptyBorder(18,18,12,18));
        p.add(title, BorderLayout.NORTH);

        String[] cols = {"Course Code","Course Title","Credits","Grade","SGPA Impact"};
        Object[][] data = {
            {"CS101","Intro to Programming",4,"A","3.6"},
            {"CS203","Data Structures",4,"A-","3.4"},
            {"MA101","Calculus I",3,"B+","3.0"}
        };
        DefaultTableModel model = new DefaultTableModel(data, cols){ @Override public boolean isCellEditable(int r,int c){ return false; } };
        JTable table = new JTable(model); table.setRowHeight(36);
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createCompoundBorder(new EmptyBorder(12,18,18,18), sp.getBorder()));
        p.add(sp, BorderLayout.CENTER);
        return p;
    }

    // Reuse header/sliding/footer implementations (minimal)
    static class HeaderBar extends JPanel { HeaderBar(){ setPreferredSize(new Dimension(0,92)); } @Override protected void paintComponent(Graphics g){ super.paintComponent(g); Graphics2D g2=(Graphics2D)g; g2.setColor(new Color(9,53,82)); g2.fillRect(0,0,getWidth(),getHeight()); int cy=getHeight()/2,left=26; g2.setColor(Color.WHITE); int barW=10,barH=44,gap=10,x=left,barY=cy-barH/2; for(int i=0;i<4;i++){ g2.fillRoundRect(x,barY,barW,barH,6,6); x+=barW+gap; } g2.setColor(new Color(85,172,168)); g2.setStroke(new BasicStroke(8f)); g2.drawArc(x-18,cy-21,42,42,-90,180); int textX=x-18+42+28; g2.setFont(new Font("Segoe UI",Font.BOLD,20)); g2.setColor(Color.WHITE); g2.drawString("INDRAPRASTHA INSTITUTE of", textX, cy-6); String line2="INFORMATION TECHNOLOGY"; g2.drawString(line2, textX, cy+20); g2.setColor(new Color(85,172,168)); g2.drawString("DELHI", textX + g2.getFontMetrics().stringWidth(line2)+10, cy+20); g2.dispose(); } }

    static class SlidingPanel extends JPanel { private final JPanel content; private final ArrowIndicator arrow; private final int EXP=300, COL=38; private boolean expanded=false; private Timer anim; SlidingPanel(JPanel inner){ content=inner; setLayout(null); setOpaque(false); setPreferredSize(new Dimension(COL,0)); arrow=new ArrowIndicator(); add(arrow); add(inner); MouseAdapter hover=new MouseAdapter(){ @Override public void mouseEntered(MouseEvent e){ expand(); } @Override public void mouseExited(MouseEvent e){ scheduleCollapse(); } }; arrow.addMouseListener(hover); addMouseListener(hover); inner.addMouseListener(hover); } private void scheduleCollapse(){ Timer t=new Timer(120,e->{ Point p=MouseInfo.getPointerInfo().getLocation(); SwingUtilities.convertPointFromScreen(p,this); if(p.x<0||p.x>getWidth()||p.y<0||p.y>getHeight()) collapse(); }); t.setRepeats(false); t.start(); } private void expand(){ if(expanded) return; expanded=true; arrow.setExpanded(true); animate(getWidth(),EXP); } private void collapse(){ if(!expanded) return; expanded=false; arrow.setExpanded(false); animate(getWidth(),COL); } private void animate(int s,int e){ if(anim!=null&&anim.isRunning()) anim.stop(); anim=new Timer(6,null); anim.addActionListener(ev->{ int c=getWidth(); int step=(e>c)?12:-12; int n=c+step; if((step>0&&n>=e)||(step<0&&n<=e)){ n=e; anim.stop(); } setPreferredSize(new Dimension(n,getHeight())); revalidate(); repaint(); }); anim.start(); } @Override public void doLayout(){ int h=getParent()!=null?getParent().getHeight():getHeight(); arrow.setBounds(0,0,COL,h); if(expanded) content.setBounds(0,0,EXP,h); else content.setBounds(COL,0,EXP,h); } static class ArrowIndicator extends JPanel { private boolean expanded=false; ArrowIndicator(){ setOpaque(false);} void setExpanded(boolean b){ expanded=b; repaint(); } @Override protected void paintComponent(Graphics g){ super.paintComponent(g); Graphics2D g2=(Graphics2D)g; g2.setColor(new Color(230,233,240)); g2.fillRect(0,0,getWidth(),getHeight()); g2.setColor(new Color(70,80,95)); int mid=getHeight()/2; if(expanded) g2.fillPolygon(new int[]{20,10,20}, new int[]{mid-10,mid,mid+10},3); else g2.fillPolygon(new int[]{10,20,10}, new int[]{mid-10,mid,mid+10},3); g2.dispose(); } } }

    static class LeftRail extends JPanel { LeftRail(){ setOpaque(false); setLayout(new BoxLayout(this, BoxLayout.Y_AXIS)); add(Box.createVerticalStrut(18)); add(pad(adminCard(),16,16,6,16)); add(Box.createVerticalStrut(12)); add(pad(navButton("My Courses", e -> new MyCoursesWindow().setVisible(true)),0,16,0,16)); add(Box.createVerticalStrut(10)); add(pad(navButton("Register Courses", e -> new RegisterCoursesWindow().setVisible(true)),0,16,0,16)); add(Box.createVerticalStrut(10)); add(pad(navButton("Profile", e -> new StudentProfileWindow().setVisible(true)),0,16,0,16)); add(Box.createVerticalStrut(10)); add(pad(navButton("Grades", e -> new ViewGradesWindow().setVisible(true)),0,16,0,16)); add(Box.createVerticalGlue()); } private JPanel adminCard(){ JPanel card=new JPanel(){ @Override protected void paintComponent(Graphics g){ super.paintComponent(g); Graphics2D g2=(Graphics2D)g; g2.setColor(Color.WHITE); g2.fillRoundRect(0,0,getWidth(),getHeight(),18,18);} }; card.setOpaque(false); card.setLayout(new BoxLayout(card,BoxLayout.Y_AXIS)); card.setBorder(new EmptyBorder(16,0,16,0)); Avatar a=new Avatar("S"); a.setAlignmentX(Component.CENTER_ALIGNMENT); JLabel name=new JLabel("Student"); name.setFont(new Font("Segoe UI",Font.BOLD,18)); name.setAlignmentX(Component.CENTER_ALIGNMENT); card.add(Box.createVerticalStrut(6)); card.add(a); card.add(Box.createVerticalStrut(8)); card.add(name); return card; } private JButton navButton(String text, ActionListener al){ JButton b=new JButton(text); b.setFont(new Font("Segoe UI",Font.PLAIN,17)); b.setBackground(Color.WHITE); b.setFocusPainted(false); b.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(223,228,234)), new EmptyBorder(10,14,10,14))); b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); b.addActionListener(al); return b; } private static JPanel pad(JComponent c,int t,int l,int b,int r){ JPanel p=new JPanel(new BorderLayout()); p.setOpaque(false); p.setBorder(new EmptyBorder(t,l,b,r)); p.add(c); return p; } static class Avatar extends JComponent { private final String initial; Avatar(String i){ initial=i; setPreferredSize(new Dimension(120,120)); } @Override protected void paintComponent(Graphics g){ Graphics2D g2=(Graphics2D)g; g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); int d=100,x=10,y=10; g2.setColor(new Color(245,248,251)); g2.fillOval(x,y,d,d); g2.setColor(new Color(225,231,238)); g2.setStroke(new BasicStroke(6f)); g2.drawOval(x+3,y+3,d-6,d-6); g2.setColor(new Color(46,56,66)); g2.setFont(new Font("Segoe UI",Font.BOLD,40)); FontMetrics fm=g2.getFontMetrics(); g2.drawString(initial, x + d/2 - fm.stringWidth(initial)/2, y + d/2 + fm.getAscent()/4); g2.dispose(); } } }

    static class FooterBar extends JPanel { FooterBar(){ setPreferredSize(new Dimension(0,56)); } @Override protected void paintComponent(Graphics g){ super.paintComponent(g); Graphics2D g2=(Graphics2D)g; g2.setColor(new Color(9,53,82)); g2.fillRect(0,0,getWidth(),getHeight()); g2.setColor(Color.WHITE); g2.setFont(new Font("Segoe UI",Font.PLAIN,15)); String[] items={"© 2024 IIT Delhi","Privacy Policy","Terms & Service"}; int x=28,y=getHeight()/2+6; for(String s:items){ g2.drawString(s,x,y); x += g2.getFontMetrics().stringWidth(s)+28; } g2.dispose(); } }
}
