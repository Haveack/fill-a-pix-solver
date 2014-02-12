package com.raid.fpsolver;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;


public class Fp {
	public final static int BLACK = 1;
	public final static int BAN = 0;
	public final static int UNDECIDED = -1;
	public final static int NONUM = -1;
	
	public final int MaxX;
	public final int MaxY;
	public final int MinX;
	public final int MinY;
	
	private Point map[][];
	private boolean flg[][];
	private int filled;
	
	public Fp(byte[] buffer) {
		int length = buffer.length;
		int cnt = 0;
		int i;
		for (i = 1; (char)buffer[i] != '\n' ; ++i) {}
		MaxY = i;
		for (int k = 1; k < length; ++k) {
			if ((char) buffer[k] == '\n') {
				cnt++;
			}
		}
		MaxX = cnt;
		filled = 0;
		//Log.i("tag", "MaxX: " + MaxX + "  MaxY: " + MaxY + "   length" + length);
		MinX = 1;
		MinY = 1;
		map = new Point[MaxX + 1][MaxY + 1];
		for (int xi = MinX, k = 0; xi <= MaxX; ++xi) {
			for (int yi = MinY; yi <= MaxY; ++yi) {
				map[xi][yi] = new Point(xi, yi, (char)buffer[k++]);
				flg[xi][yi] = false;
			}
			++k;
		}
	}
	
	public Point getPoint(int x, int y) {
		if(outBoundary(x, y))
			return null;
		return map[x][y];
	}

	public boolean outBoundary(int x, int y) {
		if (x < MinX || x > MaxX || y < MinY || y > MaxY)
			return true;
		return false;
	}

	public int getFilledCnt() {
		return filled;
	}

	public void setStateXY(int state, int x, int y) {
		if (outBoundary(x, y))
			return;
		getPoint(x, y).setState(state);
	}
	
	public void fill() {
		for (int xi = 1; xi <= MaxX; ++xi) {
			for (int yi = 1; yi <= MaxY; ++yi) {
				getPoint(xi, yi).autoFill();
			}
		}
	}
	
	public boolean solve() {
		int post_filled = -1;
		while(true) {
			if(getFilledCnt() == post_filled) {
				Log.i("tag", "stop!");
				break; 
			} 
			post_filled = getFilledCnt(); 
			fill();
		}
		if(filled == MaxX * MaxY)
			return true;
		return false;
	}
	
	public Canvas draw(Canvas canvas, int ScreenW, int ScreenH, Paint outline, Paint black, Paint ban, Paint white, Paint dkgray) {
		
		int size_w = ScreenW / MaxY;
		int size_h = ScreenH / MaxX;
		int start_w = 0;
		if (size_h < size_w) {
			size_w = size_h;
			start_w = (ScreenW - MaxY * size_w) / 2;
		}
		
		int top = 0;
		int left = start_w;
		int right = left + size_w;
		int bottom = top + size_h;
		
		canvas.drawColor(Color.WHITE);
		
		for (int xi = MinX; xi <= MaxX; ++xi) {
			for (int yi = MinX; yi <= MaxY; ++yi) {
				
				Point pi = map[xi][yi];
				switch (pi.getState()) {
				case BAN: 
					canvas.drawRect(left, top, right, bottom, ban);
				default: {
					if(pi.fulfilled == false)
						canvas.drawText(pi.getCh() + "", left + size_w / 5, bottom - size_h / 6, black);
					else 
						canvas.drawText(pi.getCh() + "", left + size_w / 5, bottom - size_h / 6, dkgray);
					break;
				}
				case BLACK: {
					canvas.drawRect(left, top, right, bottom, black);
					if(pi.fulfilled)
						canvas.drawText(pi.getCh() + "", left + size_w / 5, bottom - size_h / 6,
							ban);
					else 
						canvas.drawText(pi.getCh() + "", left + size_w / 5, bottom - size_h / 6,
								white);
					break;
				}
				}
				
				canvas.drawRect(left, top, right, bottom, outline);
				left += size_w;
				right = left + size_w;
			}
			top += size_h;
			bottom = top + size_h;
			left = start_w;
			right = left + size_w;
		}
		return canvas;
	}
		

	private int getMax(Point a, Point b, Point c) {
		int min_bc,max_ab;
		if(! c.isIn(b))
			min_bc = 0;
		else {
			min_bc = c.getNum() - c.getBlackCnt();
		}
		max_ab = b.getNum() - b.getBlackCnt() - min_bc;
		return max_ab;
	}
	
	private int getMin(Point a, Point b, Point c) {
		int max_bc, min_ab;
		max_bc = c.getNum() - c.getBlackCnt();
		if(max_bc < b.getCommonUndecidedCnt(c))
			max_bc = b.getCommonUndecidedCnt(c);
		min_ab = b.getNum() - b.getBlackCnt() - max_bc;
		if(min_ab < 0)
			min_ab = 0;
		return min_ab;
	}
	
	public class Point {
		private int x, y;
		private int state;
		private int num;
		private char ch;
		private boolean fulfilled;
		private boolean flag[][];
		private int block_size;
		private int black_max;
		
		public Point(int m_x, int m_y, char m_num) {
			x = m_x;
			y = m_y;
			ch = m_num;
			num = m_num - 48;
			if (num < 0)
				num = -1;
			state = -1;
			
			block_size = this.getBlockSize();
			black_max = num;
			
			flag = new boolean[3][3];
			
			for(int i = 0; i < 3; ++i) {
				for(int j = 0; j < 3; ++j) {
					flag[i][j] = false;
				}
			}
			fulfilled = false;
		}

		public int getX() {
			return x;
		}

		public int getY() {
			return y;
		}
		
		public int getNum() {
			return num;
		}
		
		public char getCh() {
			return ch;
		}
		
		public int getState() {
			return state;
		}

		public int getBlockSize() {
			if (x == MinX || x == MaxX || y == MinY || y == MaxY) {
				if ((x == MinX || x == MaxX) && (y == MinY || y == MaxY))
					return 4;
				else
					return 6;
			} else
				return 9;
		}

		public int getBlackCnt() {
			int cnt = 0;
			for (int xi = x - 1; xi <= x + 1; ++xi) 
				for (int yi = y - 1; yi <= y + 1; ++yi) {
					if (outBoundary(xi, yi))
						continue;
					if (map[xi][yi].getState() == BLACK)
						cnt++;
				}
			return cnt;
		}

		public int getBanCnt() {
			int cnt = 0;
			for (int xi = x - 1; xi <= x + 1; ++xi) 
				for (int yi = y - 1; yi <= y + 1; ++yi) {
					if (outBoundary(xi, yi))
						continue;
					if (map[xi][yi].getState() == BAN)
						cnt++;
				}
			
			return cnt;
		}

		public void setState(int s) {
			if (state == UNDECIDED && s != UNDECIDED )
				filled++;
			else if (state != UNDECIDED && s == UNDECIDED)
				filled--;
			state = s;
			if(this.getBlackCnt() == this.getNum())
				fulfilled = true;
		}

		public void fillBlockBlack() {
			for (int xi = x - 1; xi <= x + 1; ++xi) {
				for (int yi = y - 1; yi <= y + 1; ++yi) {
					if (outBoundary(xi, yi))
						continue;
					if (map[xi][yi].getState() == UNDECIDED)
						map[xi][yi].setState(BLACK);
				}
			}
		}

		public void fillBlockBan() {
			for (int xi = x - 1; xi <= x + 1; ++xi) {
				for (int yi = y - 1; yi <= y + 1; ++yi) {
					if (outBoundary(xi, yi))
						continue;
					if (map[xi][yi].getState() == UNDECIDED)
						map[xi][yi].setState(BAN);
				}
			}
		}

		public boolean isIn(Point p) {
			//the UNDECIDE area is totally in the area of p
			for (int xi = x - 1; xi <= x + 1; ++xi) {
				for (int yi = y - 1; yi <= y + 1; ++yi) {
					if (outBoundary(xi, yi))
						continue;
					if (map[xi][yi].getState() == UNDECIDED)
						if (!p.include(xi, yi))
							return false;
				}
			}
			return true;
		}

		public boolean include(int px, int py) {
			if(outBoundary(px, py))
				return false;
			if (Math.abs(x - px) <= 1 && Math.abs(y - py) <= 1)
				return true;
			else
				return false;
		}

		public int getRestUndecidedCntEx(Point p) {
			//count number of UNDECIDE points except the area of p
			int cnt = 0;
			for (int xi = x - 1; xi <= x + 1; ++xi) {
				for (int yi = y - 1; yi <= y + 1; ++yi) {
					if (outBoundary(xi, yi))
						continue;
					if (p.include(xi, yi))
						continue;
					if (map[xi][yi].getState() == UNDECIDED) {
						cnt++;
					}
				}
			}
			return cnt;
		}
		
		private int getCommonUndecidedCnt(Point p) {
			int cnt = 0;
			for (int xi = x - 1; xi <= x + 1; ++xi) {
				for (int yi = y - 1; yi <= y + 1; ++yi) {
					if (outBoundary(xi, yi))
						continue;
					if (p.include(xi, yi))
						if (map[xi][yi].getState() == UNDECIDED) 
						cnt++;
				}
			}
			return cnt;
		}

		public void fillRestBlackEx(Point p) {
			for (int xi = x - 1; xi <= x + 1; ++xi) 
				for (int yi = y - 1; yi <= y + 1; ++yi) {
					if (outBoundary(xi, yi))
						continue;
					if (p.include(xi, yi))
						continue;
					if (map[xi][yi].getState() == UNDECIDED) 
						map[xi][yi].setState(BLACK);
				}
		}

		public void fillRestBanEx(Point p) {
			for (int xi = x - 1; xi <= x + 1; ++xi) {
				for (int yi = y - 1; yi <= y + 1; ++yi) {
					if (outBoundary(xi, yi))
						continue;
					if (p.include(xi, yi))
						continue;
					if (map[xi][yi].getState() == UNDECIDED) {
						map[xi][yi].setState(BAN);
					}

				}
			}
		}

		private boolean check( Point b, Point c) {
			//the undecided area included by both b and c , is not included by both this and b
			for(int xi = x - 1; xi <= x + 1; ++xi) {
				for(int yi = y - 1; yi <= y +1; ++yi) {
					if(outBoundary(xi, yi))
						continue;
					if(map[xi][yi].getState() != UNDECIDED)
						continue;
					if(this.include(xi, yi) && b.include(xi, yi)) {
						if(b.include(xi, yi) && c.include(xi, yi))
							return false;
					}
				}
			}
			return true;
		}
		
		private boolean check2 (Point b, Point c) {
			//the undecided area included by b which is not included by c , is all included by this
			int bx = b.getX();
			int by = b.getY();
			for(int xi = bx - 1; xi <= bx + 1; ++xi) {
				for(int yi = by - 1; yi <= by +1; ++yi) {
					if(outBoundary(xi, yi))
						continue;
					if(map[xi][yi].getState() != UNDECIDED)
						continue;
					if(b.include(xi, yi) && !c.include(xi, yi)) {
						if(! this.include(xi, yi))
							return false;
					}
				}
			}
			return true;
		}
		
		private int getTotalRestUndecidedCnt(Point b, Point c) {
			int cnt = 0;
			for(int xi = x-1; xi <= x+1; ++xi) {
				for(int yi = y-1; yi <= y+1; ++ yi) {
					if(outBoundary(xi, yi))
						continue;
					if(!(b.include(xi, yi)) && !(c.include(xi, yi)) && map[xi][yi].state == UNDECIDED)
						cnt++;
				}
			}
			return cnt;
		}
		
		private void fillTotalRestBlack(Point b, Point c) {
			for(int xi = x-1; xi <= x+1; ++xi) {
				for(int yi = y-1; yi <= y+1; ++ yi) {
					if(outBoundary(xi, yi))
						continue;
					if(!(b.include(xi, yi)) && !(c.include(xi, yi)) && map[xi][yi].state == UNDECIDED)
						map[xi][yi].setState(BLACK);
				}
			}
		}
		
		private boolean check3(Point b, Point c) {
			for(int xi = x-1; xi <= x+1; ++xi) {
				for(int yi = y-1; yi <=y + 1; ++ yi) {
					if(outBoundary(xi, yi))
						continue;
					if(map[xi][yi].state == UNDECIDED)
						if(b.include(xi, yi) && c.include(xi, yi))
							return false;
				}
			}
			return true;
		}
		
		private void fillTotalRestBan(Point b, Point c) {
			for(int xi = x-1; xi <= x+1; ++xi) {
				for(int yi = y-1; yi <= y+1; ++ yi) {
					if(outBoundary(xi, yi))
						continue;
					if(!(b.include(xi, yi)) && !(c.include(xi, yi)) && map[xi][yi].state == UNDECIDED)
						map[xi][yi].setState(BAN);
				}
			}
		}
		
		public class Data {
			int max;
			int min;
			
			public Data(int max, int min) {
				this.max = max;
				this.min = min;
			}
			
		}
		
		/*
		public Data autoFill2() {
			Data r = new Data(0,0);
			if(this.getBlockSize() == this.getNum()) {
				this.fillBlockBlack();
				r.max = 0;
				r.min = 0;
				return r;
			}
			else if(this.getBanCnt() == this.getBlockSize() - this.getNum()) {
				this.fillBlockBan();
				r.max = 0;
				r.min = 0;
				return r;
			}
			Data[][] ri = new Data[5][5];
			for(int xi = x - 2; xi <= x + 2; ++ xi) {
				for(int yi = y -2; yi <= y + 2; ++ yi) {
					if(outBoundary(xi, yi) || flg[xi][yi] == true)
						continue;
					Point pi = 	map[xi][yi];
					ri[xi + 2 - x][yi + 2 - y] = pi.autoFill2();
				}
			}
			
			
			return r;
		}
		*/

		public void autoFill() {
			int n = this.getNum();
			if (n == NONUM) 
				return;
			int black_cnt = this.getBlackCnt();
			int ban_cnt = this.getBanCnt();
			int block_size = this.getBlockSize();

			if (black_cnt == n) {
				this.fillBlockBan();
				fulfilled = true;
				return;
			}
			if (ban_cnt == block_size - n) {
				this.fillBlockBlack();
				fulfilled = true;
				return;
			}

			for (int xi = x - 2; xi <= x + 2; ++xi) {
				for (int yi = y - 2; yi <= y + 2; ++yi) {
					if (outBoundary(xi, yi) || map[xi][yi].getNum() == -1 || (xi == x && yi == y))
						continue;
					Point pi = map[xi][yi];
					if (this.getRestUndecidedCntEx(pi) == this.getNum() - this.getBlackCnt() - pi.getNum() + pi.getBlackCnt()) {
						this.fillRestBlackEx(pi);
						//return;
					}
					else if (pi.isIn(this)) {
						if (this.getNum() - this.getBlackCnt() == pi.getNum() - pi.getBlackCnt()) {
							this.fillRestBanEx(pi);
							//return;
						}
					}
					else {
						for(int xj = xi - 2; xj <= xi + 2; ++xj) {
							for(int yj = yi -2; yj <= yi + 2; ++yj) {
								if(outBoundary(xj, yj) || map[xj][yj].getNum() == -1 || (xj == xi && yj == yi)) 
									continue;
								Point pj = map[xj][yj];
								if(! pj.isIn(pi))
									continue;
								
								if(this.check(pi, pj)) {
									int max = getMax(this, pi, pj);
									int min = getMin(this, pi, pj);
									if(this.getRestUndecidedCntEx(pi) == this.getNum() - this.getBlackCnt() - max ) {
										this.fillRestBlackEx(pi);
										return;
									}
									else if(this.check2(pi,pj))
										if(this.getNum() - this.getBlackCnt() == min) {
											this.fillRestBanEx(pi);
											return;
										}
								}
								
							}
						}
						
					}
					
					for(int xj = x - 2; xj <= x + 2; ++xj) {
						for(int yj = y -2; yj <= y + 2; ++yj) {
							if(outBoundary(xj, yj) || (xj == x && yj == y) || (xj == xi && yj == yi))
								continue;
							if(map[xj][yj].getNum() == NONUM)
								continue;
							Point pj = map[xj][yj];
							if(this.check3(pi, pj)) {
								int max_pi = pi.getNum() - pi.getBlackCnt();
								if(max_pi > this.getCommonUndecidedCnt(pi))
									max_pi = this.getCommonUndecidedCnt(pi);
								int max_pj = pj.getNum() - pj.getBlackCnt();
								if(max_pj > this.getCommonUndecidedCnt(pj))
									max_pj = this.getCommonUndecidedCnt(pj);
								if(this.getTotalRestUndecidedCnt(pi, pj) == this.getNum() - this.getBlackCnt() - max_pi - max_pj)
									this.fillTotalRestBlack(pi, pj);
								else if(pi.isIn(this) && pj.isIn(this))
									if(max_pi+max_pj == this.getNum() - this.getBlackCnt())
									this.fillTotalRestBan(pi, pj);
							}
							
						}
					}
					
				}
				
				
			}
			
		}

	}
	

}
