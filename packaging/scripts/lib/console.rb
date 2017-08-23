class Console

  def initialize
    ObjectSpace.define_finalizer( self, self.class.finalize )
  end

  def set_title(prefix, title)
    print "\033]0;#{prefix}: #{title}\007"
  end

  def clear_title
    print "\033]0;\007"
  end

  def self.finalize
    proc {
      print "\033]0;\007"
      puts "Virgo script finished execution."
    }
  end

end